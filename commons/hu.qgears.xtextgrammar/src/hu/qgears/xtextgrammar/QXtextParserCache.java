package hu.qgears.xtextgrammar;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import hu.qgears.parser.impl.ElemBufferCache;
import hu.qgears.parser.language.ILanguage;

public class QXtextParserCache extends ElemBufferCache {
	public class QXtextLang {
		ThreadLocal<RuntimeMappings> tl=new ThreadLocal<>();
		public QXtextLang(ILanguage lang, Supplier<RuntimeMappings> rmFactory) {
			this.lang=lang;
			this.rmFactory=rmFactory;
		}
		ILanguage lang;
		Supplier<RuntimeMappings> rmFactory;
		public void clearCachedRuntimeMappings() {
			tl=new ThreadLocal<>();
		}
		public RuntimeMappings getRuntimeMappings()
		{
			RuntimeMappings ret=tl.get();
			if(ret==null)
			{
				ret=rmFactory.get();
				tl.set(ret);
			}
			return ret;
		}
		public ILanguage getLang() {
			return lang;
		}
	}
	private Map<String, QXtextLang> langs=new HashMap<String, QXtextParserCache.QXtextLang>();
	
	public void registerFileType(String extension, ILanguage lang, Supplier<RuntimeMappings> rmFactory) {
		QXtextLang qlang=new QXtextLang(lang, rmFactory);
		langs.put(extension, qlang);
	}
	public QXtextLang getLang(String ext)
	{
		return langs.get(ext);
	}
	@Override
	public void waitFinish() throws InterruptedException {
		try
		{
			super.waitFinish();
		}finally
		{
			for(QXtextLang l: langs.values())
			{
				l.clearCachedRuntimeMappings();
			}
		}
	}
}
