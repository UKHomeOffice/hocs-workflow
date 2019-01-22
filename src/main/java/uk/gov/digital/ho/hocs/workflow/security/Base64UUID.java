package uk.gov.digital.ho.hocs.workflow.security;

import org.apache.commons.codec.binary.Base64;

import java.nio.ByteBuffer;
import java.util.UUID;

public class Base64UUID
{
    private Base64UUID() {}

    public static String UUIDToBase64String(UUID uuid) {
        ByteBuffer uuidBytes = ByteBuffer.wrap(new byte[16]);
        uuidBytes.putLong(uuid.getMostSignificantBits());
        uuidBytes.putLong(uuid.getLeastSignificantBits());
        return Base64.encodeBase64URLSafeString(uuidBytes.array());
    }


    public static UUID Base64StringToUUID(String base64UUIDString) {
        byte[] bytes = Base64.decodeBase64(base64UUIDString);
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        return new UUID(bb.getLong(), bb.getLong());
    }

}
