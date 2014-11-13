#include "tiffloader.h"
#include <stdio.h>
#define TIFF_BYTE_ORDER 0x4949


/*
 * Error codes
 */
char errorCodes[][100] = {
	"",//0
	"Invalid tiff file. Incorrect ImageWidth and ImageLength entries.",//1
	"Invalid tiff file byte order. Expected little endian (0x4949)",//2
	"Invalid tiff file. File is to short, header cannot be read.",//3
	"Cannot open file.",//4
	"Invalid image size in header."//5
};

const char* resolveErrorCode(int errorCode){
	if (errorCode >= 0 && errorCode < (sizeof(errorCodes)/ sizeof(errorCodes[0]))){
		return errorCodes[errorCode];
	} else {
		return "Unexpected error";
	}
}


/**
 * ImageData class implementation
 */
int getWidth(ImageData* iData){
	return iData->width;
}
int getHeight(ImageData* iData){
	return iData->height;
}

char * getPixelData(ImageData* iData){
	return iData->pixeldata;
}
void setWidth(ImageData* iData, int width_){
	iData->width = width_;
}
void setHeight(ImageData* iData,int height_){
	iData->height = height_;
}
void setPixelData(ImageData* iData,char * data_){
	iData->pixeldata = data_;
}

TiffHeader calculateTiffHeader(ImageData* iData){
	TiffHeader h;
	h.type=TIFF_BYTE_ORDER;//II for little endian
	h.reserved = 0x002a;
	h.firstIFDOffset = 8;

	h.de_count = 12;

	h.ImageWidth.tag = 0x100;
	h.ImageWidth.type = 4;
	h.ImageWidth.count = 1;
	h.ImageWidth.offset_value = getWidth(iData);

	h.ImageLength.tag = 0x101;
	h.ImageLength.type = 4;
	h.ImageLength.count = 1;
	h.ImageLength.offset_value = getHeight(iData);

	h.BitsPerSample.tag = 0x102;
	h.BitsPerSample.type = 3;
	h.BitsPerSample.count = 1;
	h.BitsPerSample.offset_value = 8;

	h.Compression.tag = 0x103;
	h.Compression.type = 3;
	h.Compression.count = 1;
	h.Compression.offset_value = 1;//no compression

	h.PhotometricInterpretation.tag = 0x106;
	h.PhotometricInterpretation.type = 3;
	h.PhotometricInterpretation.count = 1;
	h.PhotometricInterpretation.offset_value = 2;//RGB

	h.StripOffsets.tag = 0x111 ;
	h.StripOffsets.type = 3;
	h.StripOffsets.count = 1;
	h.StripOffsets.offset_value = sizeof(TiffHeader);//raw image data starts after header

	h.SamplesPerPixel.tag = 0x115;
	h.SamplesPerPixel.type = 3;
	h.SamplesPerPixel.count = 1;
	h.SamplesPerPixel.offset_value = 3;//RGB -> 3 component / pixel

	h.RowsPerStrip.tag = 0x116;
	h.RowsPerStrip.type = 4;
	h.RowsPerStrip.count = 1;
	h.RowsPerStrip.offset_value = getHeight(iData);//all rows go to same strip

	h.StripByteCounts.tag = 0x117;
	h.StripByteCounts.type = 4;
	h.StripByteCounts.count = 1;
	h.StripByteCounts.offset_value = getWidth(iData) * getHeight(iData) * 3;//all pixel in same strip

	h.XResolution.tag = 0x11A;
	h.XResolution.type = 5;
	h.XResolution.count = 1;
	h.XResolution.offset_value = sizeof(TiffHeader)-16;//pointer to resx
	h.resx = 96;
	h.resx_den = 1;

	h.YResolution.tag = 0x11B;
	h.YResolution.type = 5;
	h.YResolution.count = 1;
	h.YResolution.offset_value = sizeof(TiffHeader)-8;//pointer to resy
	h.resy = 96;
	h.resy_den = 1;

	h.ResolutionUnit.tag = 0x128;
	h.ResolutionUnit.type = 3;
	h.ResolutionUnit.count = 1;
	h.ResolutionUnit.offset_value = 2;//inch

	h.ifd_next = 0;

	return h;
}

/**
 * TiffLoader class implementations
 */
int saveImage(ImageData* image,long size ,const char* fileName){
	int errorCode = 0;
	long isize = getWidth(image) * getHeight(image) * 3;

	if (isize == size){
		FILE* myFile = fopen(fileName, "wb");
		if (myFile != NULL){
			TiffHeader header = calculateTiffHeader(image);
			fwrite (&header, sizeof(TiffHeader),1,myFile);
			fwrite (getPixelData(image), size,1,myFile);
			fclose(myFile);
		} else {
			errorCode = 4;
		}
	} else {
		errorCode = 5;
	}
	return errorCode;
}


int initializeImageData(char * fileData,long size, ImageData* image){
	int errorCode=0;
	if (size > sizeof(TiffHeader)){
		TiffHeader * header = (TiffHeader*)fileData;
		if (header->type ==  TIFF_BYTE_ORDER){
			int w = header->ImageWidth.offset_value;
			int h = header->ImageLength.offset_value;
			if (w*h*3 + sizeof(TiffHeader) == size){
				setWidth(image,w);
				setHeight(image,h);
				setPixelData(image,fileData+sizeof(TiffHeader));
			} else {
				//Invalid tiff file. Incorrect ImageWidth and ImageLength entries.
				errorCode=1;
			}
		} else {
			//Invalid tiff file byte order. Expected little endian (0x4949)
			errorCode=2;
		}
	} else {
		//Invalid tiff file. File is to short, header cannot be read.
		errorCode=3;
	}
	return errorCode;
}
