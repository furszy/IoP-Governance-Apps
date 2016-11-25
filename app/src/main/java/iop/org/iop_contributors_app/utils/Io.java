package iop.org.iop_contributors_app.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Method;

/**
 * Created by mati on 11/11/16.
 */

public class Io {

    private static final Logger log = LoggerFactory.getLogger(Io.class);

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void chmod(final File path, final int mode) {
        try {
            final Class fileUtils = Class.forName("android.os.FileUtils");
            final Method setPermissions = fileUtils.getMethod("setPermissions", String.class, int.class, int.class, int.class);
            setPermissions.invoke(null, path.getAbsolutePath(), mode, -1, -1);
        }
        catch (final Exception x) {
            log.info("problem using undocumented chmod api", x);
        }
    }

}
