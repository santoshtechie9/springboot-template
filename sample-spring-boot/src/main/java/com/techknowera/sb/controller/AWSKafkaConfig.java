import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;
import com.amazonaws.services.kafka.AWSKafka;
import com.amazonaws.services.kafka.AWSKafkaClient;

public class AWSKafkaConfig {

    public static AWSKafka createKafkaClient() {
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials("your-access-key-id", "your-secret-access-key");

        // Optionally, for temporary credentials, use STS
        // STSAssumeRoleSessionCredentialsProvider credentialsProvider = new STSAssumeRoleSessionCredentialsProvider.builder()
        //     .roleArn("arn:aws:iam::account-id:role/role-name")
        //     .roleSessionName("session-name")
        //     .build();

        AWSKafka client = AWSKafkaClient.builder()
                .credentialsProvider(new AWSStaticCredentialsProvider(awsCredentials))
                .region(Region.of("us-east-1")) // Update as per your MSK region
                .build();

        return client;
    }
}
