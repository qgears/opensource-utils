

package hu.qgears.tools.build.compiler;

import javax.tools.SimpleJavaFileObject;
import java.io.IOException;
import java.net.URI;

public class JavaStringObject extends SimpleJavaFileObject {
    private final String source;

    public JavaStringObject(String name, String source) {
        super(URI.create("string:///" + name.replaceAll("\\.", "/") +
                Kind.SOURCE.extension), Kind.SOURCE);
        this.source = source;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors)
                                                 throws IOException {
        return source;
    }
}

