package uk.gov.digital.ho.hocs.workflow.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class BaseAwsTest {
    @Autowired
    protected ObjectMapper objectMapper;

    public String getMessageMd5(Object objectToHash) {
        try{
            var hashObject = objectMapper.writeValueAsString(objectToHash);
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(hashObject.getBytes());
            byte[] digest = md.digest();
            return new BigInteger(1, digest).toString(16);
        } catch (NoSuchAlgorithmException | JsonProcessingException e) {
            return null;
        }
    }

    public static class ResultCaptor<T> implements Answer<T> {
        private T result = null;

        public T getResult() {
            return result;
        }

        @Override
        public T answer(InvocationOnMock invocationOnMock) throws Throwable {
            result = (T) invocationOnMock.callRealMethod();
            return result;
        }
    }
}
