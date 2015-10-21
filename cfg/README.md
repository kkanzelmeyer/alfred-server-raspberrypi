configExample.properties contains property fields used by Alfred. The fields  prefixed by "alfred" are fields that you may change based on your Pi's set up. Below are explanations of the fields:

**alfred.hostaddress** -> your Pi's IP Address
*alfred.hostport* -> the port for network traffic
*alfred.imagepath* -> the path to the directory where the webcam images are stored if you have a webcam
*alfred.emailclients* -> comma separated email addresses that will receive email notifications


The mail settings below are used by the Java Mail API. The example properties 
file contains values for sending through a gmail account. If you're using 
another account please adjust the settings accordingly. See the Java Mail API 
docs for common provider settings
*mail.smtp.auth*
*mail.smtp.starttls.enable*
*mail.smtp.host*
*mail.smtp.port*


The two mail settings below are the account that will actually be used to send 
emails. In Gmail, the way you can send mail from external mail clients (like 
from Alfred through the Java Mail API) is by generating an App Password for 
your google account. More info about App Passwords can be found here 
https://support.google.com/accounts/answer/185833
*mail.username* -> your gmail address
*mail.token* -> your app password
