const AWS = require('aws-sdk');
const lambda = new AWS.Lambda();

var lambdaName = process.env.LAMBDA_NAME;
var callCount = process.env.CALL_COUNT;


exports.handler = async (event) => {
    
    let promises = [];
    for (let i = 0; i < callCount; i++) {
        let promise = lambda.invoke({
                FunctionName: lambdaName,
                InvocationType: "Event",
                Payload: JSON.stringify(event)
                
        }).promise();
        promises.push(promise);
    }
    
    await Promise.all(promises).then((values) => {
      console.log(values);
    });  

    // TODO implement
    const response = {
        statusCode: 200,
        body: JSON.stringify('Hello from Lambda!'),
    };
    return response;
};
