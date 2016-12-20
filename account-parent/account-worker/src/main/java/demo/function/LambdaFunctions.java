package demo.function;

import com.amazonaws.services.lambda.invoke.LambdaFunction;
import com.amazonaws.services.lambda.model.LogType;
import demo.account.Account;

import java.util.Map;

public interface LambdaFunctions {
    
    @LambdaFunction(functionName="account-created-accountCreated-13P0EDGLDE399", logType = LogType.Tail)
    Account accountCreated(Map event);

    @LambdaFunction(functionName="accountConfirmed", logType = LogType.Tail)
    Account accountConfirmed(Map event);

    @LambdaFunction(functionName="account-activated-accountActivated-1P0I6FTFCMHKH", logType = LogType.Tail)
    Account accountActivated(Map event);

    @LambdaFunction(functionName="accountSuspended", logType = LogType.Tail)
    Account accountSuspended(Map event);

    @LambdaFunction(functionName="accountArchived", logType = LogType.Tail)
    Account accountArchived(Map event);

    @LambdaFunction(functionName="accountUnsuspended", logType = LogType.Tail)
    Account accountUnsuspended(Map event);

    @LambdaFunction(functionName="accountUnarchived", logType = LogType.Tail)
    Account accountUnarchived(Map event);
}
