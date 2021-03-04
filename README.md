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

#### DB / Cache
The general approach of this library regarding DB (and cache) is simple - it should be transparent.
That practically means that you don't ever need to manually interact with the DB.
You continue developing normally, as if there is no DB at all -
you just _Connect_ your UI _Components_ to the _Store_.

To explain how it works: you simply use the _DataTypes_ to define if some model that is normally put only
to your _Store_, should also be present in your DB.
And when fetching API, you use the _APromise's_ `withDataDispatch(persist = true)` as seen in the example above.
That's it - now you have a DB-backed _Store_, and you can forget about that DB - just keep using the _Store_ normally.

Let's see how you _extend_ a _StoreDataType_ (already present in [_Reactdroid_](https://github.com/GuyMichael/Reactdroid))
to add DB instructions to it.
Note: the current DB layer (_DbLogic_ class) uses [_ObjectBox_](https://docs.objectbox.io/getting-started) and might be changed
      to [_SqlDelight_](https://cashapp.github.io/sqldelight/) in the future, to support [_Multiplatform_](https://kotlinlang.org/lp/mobile/).
      You can, however, use any DB you like, you don't have to use _DbLogic_(!) -
      just change the callbacks' implementation and you're done.

````kotlin
    object DataTypeNetflixTitle : StoreDataType<NetflixTitle>() {
    
        override fun persistOrThrow(data: List<NetflixTitle>) {
            //DbLogic uses ObjectBox to persist
            DbLogic.persist(data.map {
                //an ObjectBox Table model
                Table_NetflixTitle.fromStoreModel(it)
            }
        }

        override fun getPersistedData(): List<NetflixTitle>? {
            return DbLogic.getAll(Table_NetflixTitle::class)
                .map { it.toStoreModel() }
        }

        override fun removeFromPersistOrThrow(data: List<NetflixTitle>) {
            //sadly, ObjectBox still doesn't allow to 'remove()' by some 'key', other than the Table id.
            // --> Otherwise, we'd define some 'uniqueKey' on the Table model that will be the same as the Store model's id.
            //Note: Table (autoincrement) id is not the same as the (Store) model id (which comes from the server).
            DbLogic.remove(data.map {
                Table_NetflixTitle.fromStoreModel(it) //we use to whole Table model to remove:/
            })
            
            //this is how we'd do it if/when ObjectBox would allow for uniqueKey removals
            DbLogic.removeByUniqueKey("modelId", data.map { it.id })
        }

        override fun clearPersistOrThrow() {
            DbLogic.removeAll(Table_NetflixTitle::class)
        }
    }
````
