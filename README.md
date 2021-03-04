# ReactdroidApp
A complete library for developing a reactive, component-based Android application.
This library is built on top of the [_Reactdroid_](https://github.com/GuyMichael/Reactdroid) architecture and adds
various features/abilities, such as network (API), DB, cache and more.

### Usage Examples
Assuming you're already familiar with [_Reactdroid_](https://github.com/GuyMichael/Reactdroid), below are
some usueful features this library adds to it:

#### Networking (API)
Using this library you can easily set multiple API clients with different domains, authentication
and other settings. It is built on top of [_Retrofit_](https://square.github.io/retrofit/). Future releases
might change the underlying library and use [_Ktor_](https://ktor.io/docs/request.html) to support [_Multiplatform_](https://kotlinlang.org/lp/mobile/).

Here is how you request some _Netflix_ API, defined with a _Retrofit_ interface.
This call returns an [_APromise_](https://github.com/GuyMichael/APromise) instance:
````kotlin
                //Retrofit interface        //String(Def) to refer to a particular ApiClient
  ApiRequest.of(ApiNetflixTitlesGet::class, ApiClientName.NETFLIX) {
      it.searchTitles() //execute the Retrofit interface method
  } //returns an APromise
````

And here is how you may connect this API to a _Reactdroid Store_,
to handle the _Dispatch_ (to the _Store_) for you. You can also use it to persist the
response/data to the DB.
````kotlin
ApiRequest.of(ApiNetflixTitlesGet::class, ApiClientName.NETFLIX) {
      it.searchTitles()
  }
  .withDataDispatch(           //an APromise extension
    DataTypeNetflixTitle       //DataType defines putting/retreiving some model from the Store (& DB)
    , { it.titles }            //map the API response model to the DataType model/type
    , merge = true             //define whether to replace new data with existing data, or merge (by IDs)
    , persist = true           //define whether to also persist the data (to DB) or not
  )
  .execute()
````

And this is how you can 'loadOrFetch', to fetch only if some data isn't already in the cache (_Store_ / DB).
````kotlin
  ApiController.loadOrFetch(
        //the DataType to look for in the Store/DB
      DataTypeNetflixTitle
        //the API request - same as before
      , { ApiRequest.of(ApiNetflixTitlesGet::class, ApiClientName.NETFLIX) {
        it.searchTitles()
      }}
        //the current state (GlobalState of the Store) to look for cached data
      , state
  ) //APromise
````
