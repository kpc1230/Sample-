package com.thed.zephyr.capture;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.thed.zephyr.capture.service.db.DynamoDBTableNameResolver;
import org.slf4j.Logger;
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by aliakseimatsarski on 8/11/17.
 */
@Configuration
@EnableDynamoDBRepositories(dynamoDBMapperConfigRef = "dynamoDBMapperConfig", basePackages = "com.thed.zephyr.capture.repositories.dynamodb")
public class DynamoDBConfig {

    @Autowired
    private Logger log;
    @Value("${amazon.dynamodb.endpoint}")
    private String amazonDynamoDBEndpoint;
    @Value("${amazon.aws.accesskey}")
    private String amazonAWSAccessKey;
    @Value("${amazon.aws.secretkey}")
    private String amazonAWSSecretKey;
    @Value("${amazon.dynamodb.local}")
    private Boolean amazonDynamoDBLocal;

    @Bean
    public DynamoDBMapperConfig dynamoDBMapperConfig(DynamoDBTableNameResolver dynamoDBTableNameResolver) {
        DynamoDBMapperConfig.Builder builder = new DynamoDBMapperConfig.Builder();
        builder.setTableNameResolver(dynamoDBTableNameResolver);
        builder.setTypeConverterFactory(DynamoDBMapperConfig.DEFAULT.getTypeConverterFactory());
        return builder.build();
    }

    @Bean
    public DynamoDBTableNameResolver dynamoDBTableNameResolver(){
        return new DynamoDBTableNameResolver();
    }

    @Bean
    public AmazonDynamoDB amazonDynamoDB(AWSCredentialsProvider awsCredentialsProvider) {
        AmazonDynamoDBClientBuilder amazonDynamoDBClientBuilder = AmazonDynamoDBClientBuilder.standard().withCredentials(awsCredentialsProvider);
        if(amazonDynamoDBLocal){
            amazonDynamoDBClientBuilder.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(amazonDynamoDBEndpoint, "us-west-2"));
        } else {
            amazonDynamoDBClientBuilder.withRegion(Regions.US_WEST_2);
        }
        AmazonDynamoDB amazonDynamoDB = amazonDynamoDBClientBuilder.build();

        return amazonDynamoDB;
    }

    @Bean
    public AWSCredentialsProvider awsCredentialsProvider() {
        AWSCredentialsProvider awsCredentialsProvider = new AWSCredentialsProvider() {
            public AWSCredentials getCredentials() {
                AWSCredentials awsCredentials = new BasicAWSCredentials(amazonAWSAccessKey, amazonAWSSecretKey);
                return awsCredentials;
            }

            public void refresh() {

            }
        };

        return awsCredentialsProvider;
    }

    @Bean
    public DynamoDB dynamoDB(AmazonDynamoDB amazonDynamoDB){
        return new DynamoDB(amazonDynamoDB);
    }
}
