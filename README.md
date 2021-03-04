# ReactdroidApp
A complete library for developing a reactive, component-based Android application.
This library is built on top of the [Reactdroid](https://github.com/GuyMichael/Reactdroid) architecture and adds
various features/abilities, such as network (API), DB, cache and more.

### Usage Examples
Assuming you're already familiar with [Reactdroid](https://github.com/GuyMichael/Reactdroid), below are
some usueful features this library adds to it:

##### Networking (API)
Using this library you can easily set multiple API clients with different domains, authentication
and other settings. It is built on top of [Retrofit](https://square.github.io/retrofit/). Future releases
might change the underlying library and use [Ktor](https://ktor.io/docs/request.html) to support [Multiplatform](https://kotlinlang.org/lp/mobile/).

Here is how you request some _Netflix_ API, defined with _Retrofit_ and this library.
This call returns an [APromise](https://github.com/GuyMichael/APromise) instance:
````kotlin
  ApiRequest.of(ApiNetflixTitlesGet::class, ApiClientName.NETFLIX) {
                //a Retrofit interface      //some String(Def) to refer to a particular ApiClient
  
      it.searchTitles() //execute the Retrofit interface method
  } //returns an APromise
````
