package com.thed.zephyr.capture;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import org.apache.commons.lang3.StringUtils;
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by aliakseimatsarski on 8/11/17.
 */
@Configuration
@EnableDynamoDBRepositories(basePackages = "com.thed.zephyr.capture.repositories.dynamodb")
public class DynamoDBConfig {

    @Value("${amazon.dynamodb.endpoint}")
    private String amazonDynamoDBEndpoint;

    @Value("${amazon.aws.accesskey}")
    private String amazonAWSAccessKey;

    @Value("${amazon.aws.secretkey}")
    private String amazonAWSSecretKey;

    @Value("${amazon.dynamodb.local}")
    private Boolean amazonDynamoDBLocal;

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
