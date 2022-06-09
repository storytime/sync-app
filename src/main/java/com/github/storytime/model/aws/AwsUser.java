package com.github.storytime.model.aws;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAutoGeneratedKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@DynamoDBTable(tableName = "app-user-prod")
public class AwsUser {

    @DynamoDBHashKey
    @DynamoDBAutoGeneratedKey
    private String id;

    @DynamoDBAttribute
    private boolean enabled;

    @DynamoDBAttribute
    private String zenAuthToken;

    @DynamoDBAttribute
    private String timeZone;

    @DynamoDBAttribute
    private long zenLastSyncTimestamp;

    @DynamoDBAttribute
    private boolean zenSyncEnabled;

    @DynamoDBAttribute
    private String ynabAuthToken;

    @DynamoDBAttribute
    private boolean ynabSyncEnable;

    @DynamoDBAttribute
    private List<AwsMerchant> awsMerchant = new ArrayList<>();

    @DynamoDBAttribute
    private List<AwsCustomPayee> awsCustomPayee = new ArrayList<>();

}
