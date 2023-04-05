# product-starter
A starter project which shows how to interact with our Developer Portal and our APIs. 
It also contains instructions for getting your product pre-listed and launched.

## Before getting started
Before doing any coding, you should pre-list your product. This is a way to get your
product into the Marketplace before it's ready to be launched. This allows
tenants to show interest in your product.

Go to https://developer.wgtwo.com, create your organization and product, then
fill out the product description, distribution and availability, support info, 
legal and privacy, and finally pricing and payment sections.

Try to make it as realistic as possible. Imagine that you are a third party
developer creating a real product. Use Google images or AI tools for
images, use ChatGPT or similar for subtitles/descriptions.

Click "Submit a Pre-Listing" once you are done, and reach out to #domain-ecosystem 
on Slack if it takes more than a few minutes after you have submitted your 
product before it's approved. Once we have approved your product, everyone in your
organization will be notified through email.

You will need to modify this product later, so you could go ahead and click the
"Create a new Version" button from the "Version and status" section on the product
summary page.

## Getting Started

If you run the commands below without modifying the code, you will be running
fully in sandbox mode. This is how developer will be spending most of their time
while integrating with us.

### Building the project

```
docker build -t product-starter .
``` 

### Running the project

```
docker run product-starter
```

### Running in production
Finish all the TODO items in `src/main/kotlin/com/wg2/examples/ProductStarterApp.kt`,
then build and run the project as described above. Detailed instructions for
each TODO item can be found in the "Configuring the project" section below.

### Configuring the project
The project has a number of variables in `src/main/kotlin/com/wg2/examples/ProductStarterApp.kt`
that need to be set to the values from your product in the Developer Portal:

| Variable | Description |
| --- | --- |
| organizationName | The name of your organization in the Developer Portal |
| productName | The name of your product in the Developer Portal |
| productSenderId | The SMS sender ID of your product in the Developer Portal. |
| productClientId | The client ID of your product in the Developer Portal |
| productClientSecret | The client secret of your product in the Developer Portal |

This readme will walk you through getting the values of each of these variables.

#### organizationName
If you haven't created an organization yet, visit https://developer.wgtwo.com/ 
and click "Create a new organization" in the top right corner.

A dialog will appear asking you to enter the name of your organization, this is
the value you should use for `organizationName`.

#### productName
Once you have created an organization, you can create a product. Visit
https://developer.wgtwo.com/organizations, click "Go to products" for
the organization you created, and then click "Create a new product" in the top right corner.

A dialog will appear asking you to enter the name of your product, this is
the value you should use for `productName`.

#### productSenderId
Once you've created a product, you can create an SMS sender ID. Visit
your product page, click the "Add Technical Integration" button, and click
"SMS Sender ID" in the left menu. Then click "Add sender ID" in the top right corner.

Choose the following values:
* Operators: Wotel
* Sender ID type: Text string
* Sender ID: <YOUR PRODUCT NAME>
* Description: I need this for the product-starter project.

#### productClientId and productClientSecret
Follow the instructions for productSenderId, but choose "OAuth clients" in 
the left menu instead. Give the client the description "Product Starter Client".
This will create a client ID and client secret. You will not be able to see 
these values again once the dialog is closed.

### Configuring your product
We need to configure your product so that it can send SMS messages.
This is done on the "Technical Integration" page of your product. You should know
how to get there by now, and you should see "Enable API scopes" in the left menu.

You can type "SMS" in the box in the top right to filter the list of scopes, then
scroll down to "SmsService wgtwo.sms.v1". We need the first two scopes:

* "sms.text:send_from_subscriber"
* "sms.text:send_to_subscriber"

These scopes are used in `src/main/kotlin/com/wg2/examples/AuthInterceptor.kt` 
when obtaining the access token used to authenticate to our APIs.

### Getting your product approved by WG2
Once you have fully configured your product, you can request approval from WG2.
This can be done by clicking the "Submit for review" button in the top right corner
of your product page.

Reach out to #domain-ecosystem on Slack if it takes more than a few minutes after
you have submitted your product before it's approved. Once we have approved your product, 
everyone in your organization will be notified through email.

### Getting someone to use your product
Once your product has been approved, people can start using it. As a third party this
isn't something you have direct control over, you will have to hope that a tenant or
subscriber will want your product, just like when you're selling an app in an app store. 
You can help by reaching out to them and telling them about your product.

#### Getting a tenant to use your product
There is a very simple way to get the Wotel tenant to use your product.
Most of you should already have access to the Wotel tenant in Partner Console.
Visit https://console.wgtwo.com/s/wotel/products and find your product in the 
"Available now" section. Click the "More" button, and spend some time reading
through the product page. This is what a tenant will see when they are looking for
products to buy.

Notice the "Permissions" section, which should reflect the scopes you have enabled
and the SMS sender ID you have configured.

Click the "Enable" button in the top right corner, and confirm in the dialog.

Your app should now output:
```
[ConsentEvents-0] INFO ConsentEventClient - New consent for tenant wotel
[ConsentEvents-0] INFO SmsClient - SMS sent: from=ProductName => to=+46724452895
```

Your product now has consent to operate on **all** the subscribers in 
the "wotel" tenant. If you click the "Disable" button on the same page, your app should output:

```
[ConsentEvents-0] INFO ConsentEventClient - Consent revoked for tenant wotel
```

If you do this, your product no longer has access to any of the subscribers in the "wotel" tenant.

If you check #jorunfa-sms-forwarding, you should see a message from your app:

```
SenderId to 072-445 28 95:
Organization Name - ProductName
Tenant 'wotel' has enabled ProductName for all their subscribers
```

Congratulations, you now know how the product/tenant relationship works!

#### Getting a subscriber to use your product
Getting a subscriber to use your product is a bit more complicated. 
This is handled through the provisioning API, in a similar way to how services
are toggled on a subscription. When a subscription gets a product provisioned,
a consent event will be fired.

You could create another product with provisioning rights, then enable that
for Wotel, then use that product to provision your original product for a subscriber, 
but asking #domain-ecosystem is probably easier. We will then add a "development consent"
through our Developer Portal Admin interface (dadmin.wgtwo.com).

You should see this output from your app:

```
[ConsentEvents-0] INFO ConsentEventClient - New consent for subscriber +46724410003
[ConsentEvents-0] INFO SmsClient - SMS sent: from=+46724410003 => to=+46724452895
```

And this in #jorunfa-sms-forwarding:

``` 
072-441 00 03 to 072-445 28 95:
Organization Name - ProductName
I, '+46724410003', have consented to use ProductName
```

Congratulations, you now know how the product/subscriber relationship works!
