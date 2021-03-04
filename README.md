# ReactdroidApp
A complete library for developing a reactive, component-based Android application.
This library is built on top of the [_Reactdroid_](https://github.com/GuyMichael/Reactdroid) architecture and adds
various features/abilities, such as network (API), DB, cache and more.

For a working app that uses this library, [just go here](https://github.com/GuyMichael/ReactdroidAppExample)

To import this project using Gradle:
```kotlin
implementation 'com.github.GuyMichael:ReactiveApp:0.1.21'
```

### Usage Examples
Assuming you're already familiar with [_Reactdroid_](https://github.com/GuyMichael/Reactdroid), below are
some usueful features this library adds to it.

#### Networking (API)
Using this library you can easily set multiple API clients with different domains, authentication
and other settings. It is built on top of [_Retrofit_](https://square.github.io/retrofit/). Future releases
might change the underlying library and use [_Ktor_](https://ktor.io/docs/request.html) to support [_Multiplatform_](https://kotlinlang.org/lp/mobile/).

Here is how you request some _Netflix_ API, defined with a _Retrofit_ interface.
This call returns an [_APromise_](https://github.com/GuyMichael/APromise) instance:
```kotlin
                //Retrofit interface        //String(Def) to refer to a particular ApiClient
  ApiRequest.of(ApiNetflixTitlesGet::class, ApiClientName.NETFLIX) {
      it.searchTitles() //execute the Retrofit interface method
  } //returns an APromise
```

And here is how you may connect this API to a _Reactdroid Store_,
to handle the _Dispatch_ (to the _Store_) for you. You can also use it to persist the
response/data to the DB.
```kotlin
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
```

And this is how you can 'loadOrFetch', to fetch only if some data isn't already in the cache (_Store_ / DB).
```kotlin
  ApiController.loadOrFetch(
        //the DataType to look for in the Store/DB
      DataTypeNetflixTitle
        //the API request - same as before
      , { ApiRequest.of(ApiNetflixTitlesGet::class, ApiClientName.NETFLIX) {
        it.searchTitles()
      }}
  ) //APromise
```

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

```kotlin
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
            //sadly, ObjectBox still doesn't allow to 'remove()' by some 'key', other than the Table id. Otherwise,
            //we'd define some 'uniqueKey' on the Table model that will be the same as the Store model's id.
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
```
That's it! You now connected your (Store) model to the DB.
'Outside world' (e.g. your UI _Components_) doesn't need to know about it!

Let's see the '`withDataDispatch(persist = true)`' example again just to make sure we got it.
So, once your _DataType_ is defined to work with your DB, this is how to persist to it from your fetch API's:
```kotlin
  ApiRequest.of(ApiNetflixTitlesGet::class, ApiClientName.NETFLIX) { it.searchTitles() }
  .withDataDispatch(           
    DataTypeNetflixTitle       
    , { it.titles }            
    , merge = true             //or false, to replace all titles that are currently in the Store/DB
    , persist = true           //or false, to ignore the DB for this call
    
    //in case there are side effects (persist/dispatch) you need to do
    //(e.g. change some state according to your UI response, regardless of the data models)
    , persistSideEffects = { /* e.g. SharedPrefLogic.setTitlesEverLoadedForAnalytics(true) */ }
    , dispatchSideEffects = { /* e.g. MainStore.dispatchIncrementTitlesLoadCount() */ }
  )
  .execute()
```

The only thing missing is defining _when_ to load some (existing) DB models - into the Store - after the app was opened.
You may do it on app-start, in which case the DB is completely transparent to you as a developer,
or do it lazy - which is helpful if there's some inner page which requires a lot of data, and that data
is not necessary outside of this page (or before it was open), thus there's no need to load it to cache (Store) on app-starts.

First, this is how you define your DB to load into the cache (Store) immediately on app-start, to be present to the UI
as soon as the user opens the app. This is done simply by defining the default-state of your _DataReducer_ -
which is already present in [_Reactdroid_](https://github.com/GuyMichael/Reactdroid).
```kotlin
    object MainDataReducer : DataReducer() {
        override fun getDefaultStatePersistenceTypes() = listOf(
            //all DataType here will be used to load their related DB Tables to the Store,
            //immediately when the app starts
            , DataTypeNetflixTitle
        )
    }
```
Yep, piece of cake. You're done.

Now let's see how you do it lazy. By the way, we already learnt how to do it in an example above:)
```kotlin
    //loadOrFetch will load your existing DB Tables to the Store - if not already present.
    //If the DB is also empty, it will execute the given 'ApiRequest'
    ApiController.loadOrFetch(
        DataTypeNetflixTitle
        , { ApiRequest.of(ApiNetflixTitlesGet::class, ApiClientName.NETFLIX) {
            it.searchTitles()
        }}
    )
    .then {
        //DB loaded (Dispatched) into the Store, or API was executed (and Dispatched into the Store)
    }
    .execute()
```
For example, you can execute this call from your UI _Component_'s `componentDidMount()` callback,
so when some data-relevant page opens, it will execute this lazy-load logic.
Note: it's better to write this logic in your feature's _Logic_ _class_ and just _execute_ it from your _Component_.
