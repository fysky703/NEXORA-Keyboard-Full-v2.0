package com.nexora.keyboard
import androidx.room.*
@Entity(tableName="clips", indices=[Index(value=["text"])])
data class Clip(@PrimaryKey(autoGenerate=true) val id:Long=0,val text:String,val pinned:Boolean=false,val favorite:Boolean=false,val folder:String="General",val createdAt:Long=System.currentTimeMillis())
@Dao interface ClipDao {
 @Query("SELECT * FROM clips ORDER BY pinned DESC, createdAt DESC") suspend fun all():List<Clip>
 @Query("SELECT * FROM clips WHERE text LIKE '%' || :q || '%' ORDER BY pinned DESC, createdAt DESC") suspend fun search(q:String):List<Clip>
 @Insert suspend fun insert(c:Clip)
 @Update suspend fun update(c:Clip)
 @Delete suspend fun delete(c:Clip)
}
@Database(entities=[Clip::class],version=1,exportSchema=false)
abstract class NexoraDb:RoomDatabase(){
 abstract fun clipDao():ClipDao
 companion object { @Volatile private var I:NexoraDb?=null
  fun get(c:android.content.Context):NexoraDb=I?:synchronized(this){I?:Room.databaseBuilder(c.applicationContext,NexoraDb::class.java,"nexora.db").build().also{I=it}}
 }
}
