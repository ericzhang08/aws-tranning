import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DeleteItemOutcome;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.UpdateItemOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.*;

public class DocumentAPIItemCRUDExample {

    static AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
    static DynamoDB dynamoDB = new DynamoDB(client);

    static String tableName = "Project_ZHANGYU";

    public static void main(String[] args) throws IOException {

        createItems();

        retrieveItem();

        update();

        // Delete the item.
        deleteItem();

        transactWriteItemsdemo();

    }

    private static void createItems() {

        Table table = dynamoDB.getTable(tableName);
        try {
            Item item = new Item().withPrimaryKey("projectName", "project120", "projectType", "projectType120")
                    .withString("mumberName", "member120");
            table.putItem(item);

        } catch (Exception e) {
            System.err.println("Create items failed.");
            System.err.println(e.getMessage());

        }
    }

    private static void retrieveItem() {
        Table table = dynamoDB.getTable(tableName);

        try {

            Item item = table.getItem("projectName", 120, "projectType", "projectType120", "projectName, projectType, mumberName", null);

            System.out.println("Printing item after retrieving it....");
            System.out.println(item.toJSONPretty());

        } catch (Exception e) {
            System.err.println("GetItem failed.");
            System.err.println(e.getMessage());
        }

    }

    private static void update() {
        Table table = dynamoDB.getTable(tableName);

        try {

            UpdateItemSpec updateItemSpec = new UpdateItemSpec().withPrimaryKey("projectName", "project120", "projectType", "projectType120")
                    .withUpdateExpression("set #na = :val1").withNameMap(new NameMap().with("#na", "mumberName"))
                    .withValueMap(new ValueMap().withString(":val1", "Some member")).withReturnValues(ReturnValue.ALL_NEW);

            UpdateItemOutcome outcome = table.updateItem(updateItemSpec);

            // Check the response.
            System.out.println("Printing item after adding new attribute...");
            System.out.println(outcome.getItem().toJSONPretty());

        } catch (Exception e) {
            System.err.println("Failed to add new attribute in " + tableName);
            System.err.println(e.getMessage());
        }
    }


    private static void deleteItem() {

        Table table = dynamoDB.getTable(tableName);

        try {

            DeleteItemSpec deleteItemSpec = new DeleteItemSpec().withPrimaryKey("projectName", "project120", "projectType", "projectType120")
                    .withReturnValues(ReturnValue.ALL_OLD);

            DeleteItemOutcome outcome = table.deleteItem(deleteItemSpec);

            // Check the response.
            System.out.println("Printing item that was deleted...");
            System.out.println(outcome.getItem().toJSONPretty());

        } catch (Exception e) {
            System.err.println("Error deleting item in " + tableName);
            System.err.println(e.getMessage());
        }
    }

    private static void transactWriteItemsdemo(){
        TransactWriteItem transactWriteItem = createTransactionItem("projectName110", "projectType110");
        TransactWriteItem transactWriteItem2 = createTransactionItem("projectName111", "projectType111");

        Collection<TransactWriteItem> actions = Arrays.asList(
                transactWriteItem, transactWriteItem2);

        TransactWriteItemsRequest placeOrderTransaction = new TransactWriteItemsRequest()
                .withTransactItems(actions)
                .withReturnConsumedCapacity(ReturnConsumedCapacity.TOTAL);
        try {
            client.transactWriteItems(placeOrderTransaction);
            System.out.println("Transaction Successful");

        } catch (ResourceNotFoundException rnf) {
            System.err.println("One of the table involved in the transaction is not found" + rnf.getMessage());
        } catch (InternalServerErrorException ise) {
            System.err.println("Internal Server Error" + ise.getMessage());
        } catch (TransactionCanceledException tce) {
            System.out.println("Transaction Canceled " + tce.getMessage());
        }
    }

    public static TransactWriteItem createTransactionItem(String projectNameVal, String projectTypeVal) {
        HashMap<String, AttributeValue> item= new HashMap<String, AttributeValue>();
        item.put("projectName", new AttributeValue(projectNameVal));
        item.put("projectType", new AttributeValue(projectTypeVal));
       return new TransactWriteItem().withPut(new Put().withItem(item).withTableName(tableName));
    }

}