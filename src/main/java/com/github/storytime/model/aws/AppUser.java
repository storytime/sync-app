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
public class AppUser {

    @DynamoDBHashKey
    @DynamoDBAutoGeneratedKey
    @DynamoDBAttribute(attributeName = "id")
    private String id;

    @DynamoDBAttribute(attributeName = "enabled")
    private boolean enabled;

    @DynamoDBAttribute(attributeName = "zenAuthToken")
    private String zenAuthToken;

    @DynamoDBAttribute(attributeName = "timeZone")
    private String timeZone;

    @DynamoDBAttribute(attributeName = "zenLastSyncTimestamp")
    private long zenLastSyncTimestamp;

    @DynamoDBAttribute(attributeName = "zenSyncEnabled")
    private boolean zenSyncEnabled;

    @DynamoDBAttribute(attributeName = "ynabAuthToken")
    private String ynabAuthToken;

    @DynamoDBAttribute(attributeName = "ynabSyncEnable")
    private boolean ynabSyncEnable;

    @DynamoDBAttribute(attributeName = "pbMerchant")
    private List<PbMerchant> pbMerchant = new ArrayList<>();

    @DynamoDBAttribute(attributeName = "customPayee")
    private List<CustomPayee> customPayee = new ArrayList<>();

}