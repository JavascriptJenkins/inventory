package com.techvvs.inventory.delivery.processor

import org.springframework.stereotype.Component


/*
This class is to process the delivery events from the queue.  At some point this might need to migrate to rabbit MQ
We will have "Deliveries" of different "Types".  This is basically an Order que but we are calling it a delivery
because the word order messes with database queries and stuff like that.

Each Delivery will have a Type which will correspond with a certain set of actions taken periodically as the delivery
is sitting in the database waiting to be processed (shipped, handed to customer in b2c retail setting, etc)

Will be used for things like:
sending payment reminders
tracking the delivery with coordinates
texting/emailing parties involved with key information and updates
etc

* */
@Component
class DeliveryProcessor {




}
