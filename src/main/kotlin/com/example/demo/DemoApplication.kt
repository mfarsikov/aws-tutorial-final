package com.example.demo

//import com.amazonaws.auth.AWSStaticCredentialsProvider
//import com.amazonaws.auth.BasicAWSCredentials
//import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.GetItemRequest
//import com.amazonaws.services.sns.AmazonSNS
//import com.amazonaws.services.sns.AmazonSNSClientBuilder
//import com.amazonaws.services.sns.model.PublishRequest
//import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
//import com.amazonaws.regions.Regions
import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import com.amazonaws.services.sqs.model.SendMessageRequest
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.runApplication
//import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
//import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder
//import com.amazonaws.services.cloudformation.AmazonCloudFormation
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Service


@SpringBootApplication
class DemoApplication

fun main(args: Array<String>) {
    runApplication<DemoApplication>(*args)
}

@RestController
class Controller(
        val ddb: AmazonDynamoDB,
//        val snsClient: AmazonSNS,
        val sqs: AmazonSQS,
        val config: Config
) {

    @GetMapping("/sum")
    fun sum(@RequestBody request: CalcRequest): CalcResponse {
        return with(request) {
            sendToSqs("$var1+$var2")
            if (isNotStored(name)) {
                storeToDynamo(name)
//                sendToSns(name)
            }

            CalcResponse(var1 + var2, LocalDateTime.now().toString(), ip())
        }
    }

    private fun sendToSqs(message: String) {
        println("QUEUE URL: ${config.sqsQueueName}")
        val queueUrl = sqs.getQueueUrl(config.sqsQueueName).queueUrl
        val sendMessageRequest = SendMessageRequest()
                .withQueueUrl(queueUrl)
                .withMessageBody(message)
        sqs.sendMessage(sendMessageRequest)
    }

    private fun storeToDynamo(name: String) {
        ddb.putItem(config.dynamoDbTableAttributeName, mapOf(config.dynamoDbTableName to AttributeValue(name)))
    }

//    private fun sendToSns(name: String) {
//        val topicArn = snsClient.createTopic(config.snsTopicName).topicArn
//        val publishRequest = PublishRequest(topicArn, "From calc: $name")
//        snsClient.publish(publishRequest)
//    }

    private fun isNotStored(name: String): Boolean {

        val request = GetItemRequest()
                .withKey(mapOf(config.dynamoDbTableAttributeName to AttributeValue(name)))
                .withTableName(config.dynamoDbTableName)

        return ddb.getItem(request).item.isNotEmpty()
    }


    private fun ip() = "127.0.0.1" //todo: calcmodel.ip()

}

data class CalcRequest(
        val var1: Int,
        val var2: Int,
        val name: String
)

data class CalcResponse(
        val result: Int,
        val dateTime: String,
        val ip: String
)

@Configuration
@ConfigurationProperties
class Config {
    lateinit var awsAccessKeyId: String
    lateinit var awsSecretKey: String
    lateinit var sqsQueueName: String
    lateinit var snsTopicName: String
    lateinit var dynamoDbTableName: String
    lateinit var dynamoDbTableAttributeName: String
}

@Configuration
class DynamoConfig {


/*
    @Bean
    fun credentials(config: Config) = BasicAWSCredentials(config.awsAccessKeyId, config.awsSecretKey)
*/


/*    @Bean
    fun getSqs(credentials: BasicAWSCredentials) = AmazonSQSClientBuilder.standard()
            .withCredentials(AWSStaticCredentialsProvider(credentials))
            .withRegion(Regions.US_EAST_2)
            .build()*/

    @Bean
    fun getDynamoDb(/*credentials: BasicAWSCredentials*/) =
            AmazonDynamoDBClientBuilder.defaultClient()//.standard()
               /*     .withCredentials(AWSStaticCredentialsProvider(credentials))
                    .withRegion(Regions.US_WEST_2)
                    .build()*/
/*
    @Bean
    fun getSnsClient(credentials: BasicAWSCredentials) = AmazonSNSClientBuilder.standard()
            .withCredentials(AWSStaticCredentialsProvider(credentials))
            .withRegion(Regions.US_WEST_2)
            .build()*/

}
