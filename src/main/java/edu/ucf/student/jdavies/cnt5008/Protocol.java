package edu.ucf.student.jdavies.cnt5008;

import com.google.protobuf.ExtensionRegistry;
import edu.ucf.student.jdavies.cnt5008.proto.Proto;

/**
 * Helper method for registring up extensions to our Protobuf Proto.
 */
public class Protocol {
    private static Protocol instance = null;
    private static ExtensionRegistry extensionRegistry = ExtensionRegistry.newInstance();

    public synchronized  static Protocol getInstance() {
        if (instance == null) {
            instance = new Protocol();
            Proto.registerAllExtensions(extensionRegistry);
        }
        return instance;
    }
}
