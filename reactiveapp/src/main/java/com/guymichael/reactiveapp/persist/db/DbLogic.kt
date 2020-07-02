package com.guymichael.reactiveapp.persist.db

import com.guymichael.kotlinreact.Logger
import com.guymichael.reactiveapp.BuildConfig
import io.objectbox.Box
import io.objectbox.BoxStore
import io.objectbox.Property
import io.objectbox.query.OrderFlags
import kotlin.reflect.KClass

//TODO data browser: https://docs.objectbox.io/data-browser
object DbLogic {
    @Volatile private lateinit var boxStore: BoxStore

    @JvmStatic
    fun init(box: BoxStore) {
        boxStore = box
    }

    private fun <T> boxOf(cls: Class<T>): Box<T>? {
        return boxStore.boxFor(cls)
    }

    @JvmStatic
    fun <T> getAll(cls: Class<T>): List<T> {
        return boxOf(cls)?.all.also {

            Logger.d(DbLogic::class
                , if (it == null) "failure loading all records of ${cls.simpleName} (null)"
                else "loaded ${it.size} ${cls.simpleName} records successfully"
            )

            if (it != null && BuildConfig.DEBUG && it.size <= 10) {
                Logger.d(DbLogic::class, it.toString())
            }

        } ?: emptyList()
    }

    fun <T : Any> getAll(cls: KClass<T>): List<T> {
        return getAll(cls.java)
    }

    fun <T> get(cls: Class<T>, id: Long): T? {
        return boxOf(cls)?.get(id).also {

            Logger.d(DbLogic::class
                , if (it == null) "failure loading single ${cls.simpleName} (null)"
                else "loaded single ${cls.simpleName} successfully"
            )

            if (it != null && BuildConfig.DEBUG) {
                Logger.d(DbLogic::class, it.toString())
            }
        }
    }

    fun <T : Any> get(cls: KClass<T>, id: Long): T? {
        return get(cls.java, id)
    }

    fun <T> getSorted(cls: Class<T>, property: Property<T>, limit: Long, ascending: Boolean = true): List<T> {
        return boxOf(cls)
            ?.query()
            ?.order(property, if (ascending) 0 else OrderFlags.DESCENDING)
            ?.build()
            ?.find(0, limit).also {

                Logger.d(DbLogic::class
                    , if (it == null) "failure loading sorted list of ${cls.simpleName} (null)"
                    else "loaded ${it.size} ${cls.simpleName} sorted records successfully (by property ${property.name})"
                )

                if (it != null && BuildConfig.DEBUG && it.size <= 10) {
                    Logger.d(DbLogic::class, it.toString())
                }
            }
            ?: emptyList()
    }

    fun <T : Any> getByProperty(entity: KClass<T>, property: Property<T>, propertyValue: Long): T? {
        return boxOf(entity.java)
            ?.query()
            ?.equal(property, propertyValue)
            ?.build()
            ?.findFirst().also {

                Logger.d(DbLogic::class
                    , if (it == null) "failure loading ${entity.simpleName} (null)"
                    else "loaded single ${entity.simpleName} successfully (by property ${property.name})"
                )

                if (it != null && BuildConfig.DEBUG) {
                    Logger.d(DbLogic::class, it.toString())
                }
            }
    }

    fun <T: Any> persist(entity: T): Long? {
        return try {
            boxOf(entity.javaClass)?.put(entity).also { id ->

                Logger.d(DbLogic::class, "persisted single ${entity.javaClass.simpleName} " +
                    (if (id == null) "with failure: no id" else "successfully (id = $id)")
                )

                if (BuildConfig.DEBUG) {
                    Logger.d(DbLogic::class, entity.toString())
                }

            }
        } catch (e: Exception) {
            //put failed, e.g. due to duplicate primary(unique) key
            e.printStackTrace()
            null
        }
    }

    fun <T: Any> persist(vararg entities: T): Boolean {
        return persist(listOf(entities))
    }

    fun <T: Any> persist(list: List<T>): Boolean {
        return try {
            (boxOf(list.first().javaClass)?.let {
                it.put(list)
                true
            })?.also {

                Logger.d(DbLogic::class
                    , "persisted ${list.size} ${list.first().javaClass.simpleName} records"
                )

                if (BuildConfig.DEBUG && list.size <= 10) {
                    Logger.d(DbLogic::class, it.toString())
                }

            } ?: false
        } catch (e: Exception) {
            //put failed, e.g. due to duplicate primary(unique) key OR due to an empty 'list'
            e.printStackTrace()
            //THINK how will the db look like? Will it rollback? Will it leave already persisted entities?
            false
        }
    }

    /** Note: it does NOT remove related entities!!! (e.g. ToOne), see here: https://github.com/objectbox/objectbox-java/issues/79 */
    fun <T: Any> remove(list: List<T>): Boolean {
        return try {
            (boxOf(list.first().javaClass)?.let {
                it.remove(list)
                true
            } ?: false).also {

                Logger.d(DbLogic::class
                    , "removed (up to) ${list.size} ${list.first().javaClass.simpleName} records"
                )

                if (BuildConfig.DEBUG && list.size <= 10) {
                    Logger.d(DbLogic::class, it.toString())
                }

            }
        } catch (e: Exception) {
            //remove failed, e.g. due to an empty 'list'
            false
        }
    }

    /** Note: it does NOT remove related entities!!! (e.g. ToOne), see here: https://github.com/objectbox/objectbox-java/issues/79 */
    fun <T: Any> remove(entity: T): Boolean {
        return try {
            (boxOf(entity.javaClass)?.let {
                remove(entity)
                true
            })?.also {

                Logger.d(DbLogic::class, "removed single ${entity.javaClass.simpleName} record")

                if (BuildConfig.DEBUG) {
                    Logger.d(DbLogic::class, entity.toString())
                }

            } ?: false
        } catch (e: Exception) {
            //remove failed, e.g. due to not found
            false
        }
    }

    /** Note: it does NOT remove related entities!!! (e.g. ToOne), see here: https://github.com/objectbox/objectbox-java/issues/79 */
    fun <T: Any> removeAll(table: KClass<T>): Boolean {
        return try {
            (boxOf(table.java)?.let {
                it.removeAll()
                true
            } ?: false).also {

                Logger.d(DbLogic::class, "removed all records from table ${table.simpleName}")
            }
        } catch (e: Exception) {
            //remove failed, e.g. due to not found
            false
        }
    }

    fun deleteDb() {
        boxStore.removeAllObjects()

        Logger.d(DbLogic::class, "deleted and restarted entire db")
    }
}