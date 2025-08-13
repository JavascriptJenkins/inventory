package com.techvvs.inventory.model.nonpersist


class RequestMetaData {

    String uri // what uri are we submitting too
    String httpMethod // POST, UPDATE, etc.
    String baseUri
    Object responseObjectType // what type of object are we expecting back

}
