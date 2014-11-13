#include <stdio.h>
#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libswscale/swscale.h>

#include "generated/com_rizsi_video_ffmpeg_NativeVideo.h"
#define METHODPREFIX(PARAMTYPE, METHODNAME) JNIEXPORT PARAMTYPE JNICALL \
	Java_com_rizsi_video_ffmpeg_NativeVideo_##METHODNAME
#define ST_ARGS JNIEnv * env, jclass clazz
#define VIDEO_MAX_STRING 1024 

// Useful libavcodec documentation page: http://www.dranger.com/ffmpeg/functions.html

// TODO seek to frame tutorial: http://www.dranger.com/ffmpeg/tutorial07.html
typedef struct 
{
	int width;
	int height;
	int targetWidth;
	int targetHeight;
	AVRational frameRate;
	AVFormatContext *pFormatCtx;
	AVStream * videoStream;
	int             i, videoStreamIndex;
	AVCodecContext  *pCodecCtx;
	AVCodec         *pCodec;
	AVFrame         *pFrame;
	AVFrame         *pFrameRGB;
	uint8_t         *buffer;
	int				bufferSize;
	int targetFormat;
	char lastError[VIDEO_MAX_STRING];
	char fileName[VIDEO_MAX_STRING];
	jlong lastErrorCode;
	struct SwsContext *img_convert_ctx;
} videoStruct;

/**
 * convert pointer to jlong value
 */
jlong ptrToLong(void * ptr)
{
	return (jlong) ptr;
}
jlong getAvTime()
{
	return av_gettime();
}
/**
 * convert long value to pointer
 */
void * longToPtr(jlong l)
{
	return (void *) l;
}
// SEE docs in Java interface
METHODPREFIX(jstring, getLastError)(ST_ARGS, jlong ptr)
{
	videoStruct * vid=longToPtr(ptr);
	jstring ret=(*env)->NewStringUTF(env, vid->lastError); 
	return ret;
}
// SEE docs in Java interface
METHODPREFIX(jlong, getLastErrorCode)(ST_ARGS, jlong ptr)
{
	videoStruct * vid=longToPtr(ptr);
	return vid->lastErrorCode;
}
// SEE docs in Java interface
METHODPREFIX(jlong, initVideoLib)(ST_ARGS)
{
	// Register all formats and codecs
	printf("initializing video");
	av_register_all();
	return (jlong)0;
}
// SEE docs in Java interface
METHODPREFIX(jlong, createStream)(ST_ARGS)
{
	// Create own structure to store all about the video object that is created now.
	// All pointers are NULLed so closeStream will know which resources must be freed
	videoStruct * vid=calloc(1, sizeof(videoStruct));
	return ptrToLong(vid);
}

/**
 * Get the video buffer pointer from the long representation of the pointer.
 * Also clear the last error state of the vid structure.
 * It is a practical first method of all stream handling functions.
 */
videoStruct * getVid(jlong ptr)
{
	videoStruct * vid=longToPtr(ptr);
	strcpy(vid->lastError, "");
	vid->lastErrorCode=0;
}
// SEE docs in Java interface
METHODPREFIX(jlong, getImageBufferSize)(ST_ARGS, jlong ptr) 
{
	videoStruct * vid=getVid(ptr);
	return vid->bufferSize;
}
// SEE docs in Java interface
METHODPREFIX(jobject, getImageBuffer) (ST_ARGS, jlong ptr)
{
	videoStruct * vid=getVid(ptr);
	void * address = vid->buffer;
	jlong capacity=vid->bufferSize;
	jobject ret=(*env)->NewDirectByteBuffer(env, address, capacity);
	return ret;
}
// SEE docs in Java interface
METHODPREFIX(jobject, getNativeImageBuffer) (ST_ARGS, jlong ptr, jint index)
{
	videoStruct * vid=getVid(ptr);
	void * address = vid->pFrame->data[index];
	jlong capacity=vid->pFrame->linesize[index]*vid->height;
	if(index>0)
		capacity/=2;
	jobject ret=(*env)->NewDirectByteBuffer(env, address, capacity);
	return ret;
}
// SEE docs in Java interface
METHODPREFIX(jlong, openFile) (ST_ARGS, jlong ptr, jstring fileNameString, jboolean convertToPowOfTwo)
{
	videoStruct * vid=getVid(ptr);
	
	// The target format is rgba in our openGL contexts
	// TODO we may spare some memory cycles by using RGB without "A"
	vid->targetFormat=PIX_FMT_RGB24;

	// TODO check filename length (in bytes)
	const char * fileNameBytes=(*env)->GetStringUTFChars(
		env, fileNameString, NULL);
	strcpy(vid->fileName, fileNameBytes);
	(*env)->ReleaseStringUTFChars(env, fileNameString, fileNameBytes);
	
	
	// TODO remove log 
	printf("Native opening file: %s\n", vid->fileName); fflush(stdout);
	
	// Open video file with libavcodec
	vid->lastErrorCode=av_open_input_file(&(vid->pFormatCtx), vid->fileName, NULL, 0, NULL);
	if(vid->lastErrorCode!=0)
	{
		// Couldn't open file
		// TODO copy libavcodec error to be accessible from Java
		strcpy(vid->lastError, "Could not open file for unknown reason");
		return -1l;
	}
	// Retrieve stream information
	vid->lastErrorCode=av_find_stream_info(vid->pFormatCtx);
	if(vid->lastErrorCode<0)
	{
		// Couldn't find stream information
		// TODO copy libavcodec error to be accessible from Java
		strcpy(vid->lastError, "Couldn't find stream information");
		return -2l;
	}
	
	// TODO remove Dump information about file onto standard error
	dump_format(vid->pFormatCtx, 0, vid->fileName, 0);

	// Find the first video stream
	vid->videoStreamIndex=-1;
	int i;
	for(i=0; i<vid->pFormatCtx->nb_streams; i++)
	{
		if(vid->pFormatCtx->streams[i]->codec->codec_type==CODEC_TYPE_VIDEO)
		{
			vid->videoStream=vid->pFormatCtx->streams[i];
			vid->videoStreamIndex=i;
			break;
		}
	}
	if(vid->videoStreamIndex==-1)
	{
		// TODO copy libavcodec error to be accessible from Java
		strcpy(vid->lastError, "Couldn't find video stream in file");
		return -3l; // Didn't find a video stream
	}
	strcpy(vid->lastError, "File opened!");
	
	// Get a pointer to the codec context for the video stream
	vid->pCodecCtx=vid->pFormatCtx->streams[vid->videoStreamIndex]->codec;
	// Find the decoder for the video stream
	vid->pCodec=avcodec_find_decoder(vid->pCodecCtx->codec_id);
	if(vid->pCodec==NULL)
	{
		// TODO copy libavcodec error to be accessible from Java
		strcpy(vid->lastError, "Codec not found for video stream");
		return -4l; // Codec not found
	}
	// Open codec
	vid->lastErrorCode=avcodec_open(vid->pCodecCtx, vid->pCodec);
	if(vid->lastErrorCode<0)
	{
		// TODO copy libavcodec error to be accessible from Java
		strcpy(vid->lastError, "Codec for video found but could not be opened");
		return -5l; // Could not open codec
	}
	// Fill the globally readable values in our structure
	vid->width=vid->pCodecCtx->width;
	vid->height=vid->pCodecCtx->height;
	if(convertToPowOfTwo)
	{
		vid->targetHeight=1;
		while(vid->targetHeight<vid->height)
		{
			vid->targetHeight*=2;
		}
		vid->targetWidth=1;
		while(vid->targetWidth<vid->width)
		{
			vid->targetWidth*=2;
		}
	}else
	{
		vid->targetHeight=vid->height;
		vid->targetWidth=vid->width;
	}
	// TODO query framerate
	vid->frameRate=vid->videoStream-> r_frame_rate;
	
	// Allocate video frame for decoded frame
	vid->pFrame=avcodec_alloc_frame();
	if(vid->pFrame==NULL)
	{
		// TODO log error and cleanup stuctures
		return -6l;
	}
	
	// Allocate an AVFrame structure for RBGA converted frame 
	vid->pFrameRGB=avcodec_alloc_frame();
	if(vid->pFrameRGB==NULL)
	{
		// TODO log error and cleanup stuctures
		return -7l;
	}

	// TODO find documentation of the filters and find the appropriate
	int filterType=SWS_POINT; //SWS_BICUBIC
	vid->img_convert_ctx=sws_getContext(
		vid->pCodecCtx->width, vid->height,
		vid->pCodecCtx->pix_fmt, vid->targetWidth, vid->targetHeight,
		vid->targetFormat,
		filterType,
		NULL, NULL, NULL);
	if(vid->img_convert_ctx==NULL)
	{
		strcpy(vid->lastError, "Error creating frame format conversion context");
		return -8l;
	}

	int lineSize=vid->targetWidth*3;
	// TODO where is alignment to 4 specified in libavformat documentation?
	while(lineSize%4)
	{
		lineSize++;
	}
	// WARNING: avpicture_get_size does not handle alignment on 4 fine
	// avpicture_get_size(vid->targetFormat, vid->targetWidth, vid->targetHeight);
	vid->bufferSize=lineSize*vid->targetHeight;
	vid->buffer=av_malloc(vid->bufferSize);
	strcpy(vid->lastError, "ok");
	return 0l;
}
// SEE docs in Java interface
METHODPREFIX(jlong, seekToFrame) (ST_ARGS, jlong ptr, jlong frameIndex)
{
	videoStruct * vid=getVid(ptr);
	av_seek_frame(vid->pFormatCtx,
		vid->videoStreamIndex,
		// TODO time base may not equal to frame time
		frameIndex,
		0);
	avcodec_flush_buffers(vid->pCodecCtx);
}
// SEE docs in Java interface
METHODPREFIX(jlong, closeStream) (ST_ARGS, jlong ptr)
{
	videoStruct * vid=getVid(ptr);
	
	// Free the RGB image
	if(vid->pFrameRGB!=NULL)
	{
		av_free(vid->pFrameRGB);
		vid->pFrameRGB=NULL;
	}
	if(vid->buffer!=NULL)
	{
		av_free(vid->buffer);
		vid->buffer=NULL;	
	}
	// Free the YUV frame
	if(vid->pFrame!=NULL)
	{
		av_free(vid->pFrame);
		vid->pFrame=NULL;
	}
	// Close the codec
	if(vid->pCodecCtx!=NULL)
	{
		avcodec_close(vid->pCodecCtx);
		vid->pCodecCtx=NULL;
	}
    // Close the video file
    if(vid->pFormatCtx!=NULL)
    {
    	av_close_input_file(vid->pFormatCtx);
    	vid->pFormatCtx=NULL;
    }
    if(vid->img_convert_ctx!=NULL)
    {
    	sws_freeContext(vid->img_convert_ctx);
    	vid->img_convert_ctx=NULL;
    }
	// TODO free resources allocated for the stream - check whether we freed all or not!
	free(vid);
}
// SEE docs in Java interface
METHODPREFIX(jdouble, getFrameRate) (ST_ARGS, jlong ptr)
{
	videoStruct * vid=getVid(ptr);
	return  av_q2d(vid->frameRate);
}
// SEE docs in Java interface
METHODPREFIX(jlong, getNextFrame) (ST_ARGS, jlong ptr, jboolean convertToRgb)
{
	videoStruct * vid=getVid(ptr);
	int i=0;
	AVPacket        packet;
	jlong ret=0;

	while(av_read_frame(vid->pFormatCtx, &packet)>=0)
	{
		// Is this a packet from the video stream?
		int frameFinished=0;
		if(packet.stream_index==vid->videoStreamIndex)
		{
			// Decode video frame
			avcodec_decode_video(vid->pCodecCtx, vid->pFrame, &frameFinished,
				packet.data, packet.size);
		}
        // Free the packet that was allocated by av_read_frame
        av_free_packet(&packet);
		// Did we get a video frame?
		if(frameFinished)
		{
			if(convertToRgb)
			{
				// Assign appropriate parts of buffer to image planes in pFrameRGB

				AVPicture * frameYUV=(AVPicture *)(vid->pFrame);
				AVPicture * frameRGB=(AVPicture*)(vid->pFrameRGB);
				avpicture_fill(frameRGB, vid->buffer, vid->targetFormat,
					vid->targetWidth, vid->targetHeight);
				sws_scale(vid->img_convert_ctx,
					 frameYUV->data, frameYUV->linesize, 0,
				 	vid->height,
				 	frameRGB->data, frameRGB->linesize);
			}
			strcpy(vid->lastError, "ok");
           	return 0;
		}
	}
	// No more frames could be read from file
	strcpy(vid->lastError, "File ended");
	return 1;
}

METHODPREFIX(jint, getWidth) (ST_ARGS, jlong ptr)
{
	videoStruct * vid=getVid(ptr);
	return vid->width;
}
METHODPREFIX(jint, getHeight) (ST_ARGS,jlong ptr)
{
	videoStruct * vid=getVid(ptr);
	return vid->height;
}
METHODPREFIX(jint, getTargetWidth) (ST_ARGS, jlong ptr)
{
	videoStruct * vid=getVid(ptr);
	return vid->targetWidth;
}
METHODPREFIX(jint, getTargetHeight) (ST_ARGS,jlong ptr)
{
	videoStruct * vid=getVid(ptr);
	return vid->targetHeight;
}
