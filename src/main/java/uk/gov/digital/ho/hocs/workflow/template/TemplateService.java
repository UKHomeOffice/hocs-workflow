package uk.gov.digital.ho.hocs.workflow.template;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.workflow.infoClient.InfoClient;
import uk.gov.digital.ho.hocs.workflow.model.CaseType;

import java.io.IOException;

@Slf4j
@Service
public class TemplateService {

    private String bucketName;
    private AmazonS3 s3client;
    private final InfoClient infoClient;

    @Autowired
    public TemplateService(InfoClient infoClient,
                           @Value("${aws.bucketname}") String bucketName,
                           @Value("${aws.accesskey}") String accessKey,
                           @Value("${aws.secretkey}") String secretKey) {
        this.infoClient = infoClient;
        this.bucketName = bucketName;
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        this.s3client = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.EU_WEST_2)
                .build();
    }

    public ResponseEntity<ByteArrayResource> getTemplateForCaseType(CaseType caseType) {
        // get key from info service
        String documentKey = infoClient.getTemplate(caseType).getDocumentKey();
        //use key to return template
        if (s3client.doesObjectExist(bucketName, documentKey)) {
            log.info("template exists - request download for template with key {}", documentKey);
            S3Object s3Object = s3client.getObject(bucketName, documentKey);
            byte[] byteArray = new byte[0];
            try {
                byteArray = IOUtils.toByteArray(s3Object.getObjectContent());
            } catch (IOException e) {
                log.error("Error converting s3object to byteArray - templateKye {}", documentKey);
            }
            String fileName = s3Object.getObjectMetadata().getUserMetadata().get("originalname");
            MediaType mediaType = MediaType.valueOf(s3Object.getObjectMetadata().getContentType());
            ByteArrayResource resource = new ByteArrayResource(byteArray);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName)
                    .contentType(mediaType) //
                    .contentLength(byteArray.length) //
                    .body(resource);
        } else {
            log.info("template {} dosent exist", documentKey);
            return ResponseEntity.notFound().build();
        }
    }
}

