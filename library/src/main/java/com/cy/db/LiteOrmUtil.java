package com.cy.db;

import com.cy.app.UtilContext;
import com.cy.data.UtilCollection;
import com.litesuits.orm.LiteOrm;
import com.litesuits.orm.db.DataBaseConfig;
import com.litesuits.orm.db.assit.QueryBuilder;
import com.litesuits.orm.db.assit.WhereBuilder;
import com.litesuits.orm.db.model.ColumnsValue;
import com.litesuits.orm.db.model.ConflictAlgorithm;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class LiteOrmUtil {
    private volatile static LiteOrm mLiteOrm;
    private static DataBaseConfig sDatabaseConfig;

    public static void initDatabaseConfig(DataBaseConfig databaseConfig) {
        sDatabaseConfig = databaseConfig;
    }

    protected static LiteOrm getDB() {
        if (mLiteOrm == null) {
            synchronized (LiteOrmUtil.class) {
                if (mLiteOrm == null) {
                    // 使用级联操作
                    if (sDatabaseConfig == null) {
                        sDatabaseConfig = new DataBaseConfig(
                                UtilContext.getContext(),
                                DataBaseConfig.DEFAULT_DB_NAME,
                                false,
                                DataBaseConfig.DEFAULT_DB_VERSION,
                                null);
                    }
                    mLiteOrm = LiteOrm.newCascadeInstance(sDatabaseConfig);
                }
            }
        }
        return mLiteOrm;
    }

    public static long save(Object object) {
        return getDB().save(object);
    }

    public static <T> void save(Collection<T> collection) {
        getDB().save(collection);
    }

    public static <T> ArrayList<T> query(Class<T> obj) {
        return getDB().query(obj);
    }

    public static void update(Object object) {
        getDB().update(object);
    }

    public static <T> int update(Collection<T> collection) {
        return getDB().update(collection);
    }

    public static void delete(Object object) {
        getDB().delete(object);
    }

    public static <T> void delete(Class<T> var1) {
        getDB().delete(var1);
    }

    public static <T> int delete(Collection<T> collection) {
        return getDB().delete(collection);
    }

    /**
     * 删除所有 某字段等于 Vlaue的值
     */
    public static <T> void deleteWhere(Class<T> cla, String field, String value) {
        getDB().delete(cla, WhereBuilder.create(cla).where(field + "=?", value));
    }

    public static <T> int update(Class<T> cla, String whereColumn, Object whereArgs,
                                 String updateColumn, Object toValue) {
        int result = getDB()
                .update(WhereBuilder.create(cla)
                                .where(whereColumn + "=?", new Object[]{whereArgs}),
                        new ColumnsValue(new String[]{updateColumn}, new Object[]{toValue}),
                        ConflictAlgorithm.Replace
                );
        return result;
    }

    public static <T> int updateOrCreate(Class<T> cla, String whereColumn, String whereArgs,
                                         String updateColumn, Object toValue){
        int result=update(cla,whereColumn,whereArgs,updateColumn,toValue);
        if (result>0){

        }else {
            try {
                T t = cla.newInstance();
                Field fieldWhere=cla.getDeclaredField(whereColumn);
                fieldWhere.setAccessible(true);
                fieldWhere.set(t,whereArgs);
                Field field=cla.getDeclaredField(updateColumn);
                field.setAccessible(true);
                field.set(t,toValue);
                result = (int) save(t);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static <T> int updateOrCreate(Class<T> cla, Map<String,Object> whereKVs,
                                         String updateColumn, Object toValue){
        int result=update(cla,whereKVs,updateColumn,toValue);
        if (result<=0){
            try {
                T t = cla.newInstance();

                for (Map.Entry<String, Object> entry : whereKVs.entrySet()) {
                    Field fieldWhere=cla.getDeclaredField(entry.getKey());
                    fieldWhere.setAccessible(true);
                    fieldWhere.set(t,entry.getValue());
                }

                Field field=cla.getDeclaredField(updateColumn);
                field.setAccessible(true);
                field.set(t,toValue);
                result = (int) save(t);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static <T> int update(Class<T> cla, String whereColumn1, String whereArgs1,
                                 String whereColumn2, String whereArgs2,
                                 String updateColumn, Object toValue) {
        int result = getDB()
                .update(WhereBuilder.create(cla)
                                .where(whereColumn1 + "=?", new Object[]{whereArgs1})
                                .and(whereColumn2 + "=?", new Object[]{whereArgs2}),
                        new ColumnsValue(new String[]{updateColumn}, new Object[]{toValue}),
                        ConflictAlgorithm.Replace
                );
        return result;
    }

    public static <T> int update(Class<T> cla,Map<String,Object> whereKVs,String updateColumn, Object toValue){
        int result = 0;
        if (UtilCollection.notEmpty(whereKVs)){
            WhereBuilder whereBuilder=new WhereBuilder(cla);
            boolean addedWhere=false;
            for (Map.Entry<String, Object> entry : whereKVs.entrySet()) {
                entry.getKey();
                entry.getValue();
                if (!addedWhere) {
                    addedWhere=true;
                    whereBuilder.where(entry.getKey()+ "=?",entry.getValue());
                }else {
                    whereBuilder.and(entry.getKey()+ "=?",entry.getValue());
                }
            }
            result = getDB().<T>update(whereBuilder,
                    new ColumnsValue(new String[]{updateColumn}, new Object[]{toValue}),
                    ConflictAlgorithm.Replace);
        }
        return result;
    }

    public static <T> int update(Class<T> cla, String whereColumn, Object whereArgs,
                                 Object toValue) {
        return update(cla, whereColumn, whereArgs, whereColumn, toValue);
    }

    /**
     * 查询  某字段 等于 Value的值
     */
    public static <T> List<T> query(Class<T> cla, String field, Object value) {
        return getDB().<T>query(new QueryBuilder(cla).where(field + "=?", value));
    }

    /**
     * 查询  某字段 等于 Value的值
     */
    public static <T> List<T> query(Class<T> cla, Map<String,Object> kvs) {
        if (UtilCollection.notEmpty(kvs)){
            QueryBuilder queryBuilder=new QueryBuilder(cla);
            boolean addedWhere=false;
            for (Map.Entry<String, Object> entry : kvs.entrySet()) {
                entry.getKey();
                entry.getValue();
                if (!addedWhere) {
                    addedWhere=true;
                    queryBuilder.where(entry.getKey()+ "=?",entry.getValue());
                }else {
                    queryBuilder.whereAnd(entry.getKey()+ "=?",entry.getValue());
                }
            }
            return getDB().<T>query(queryBuilder);
        }
        return null;
    }

    /**
     * 查询  某字段 等于 Value的值
     */
    public static <T> T querySingle(Class<T> cla, Map<String,Object> kvs) {
        if (UtilCollection.notEmpty(kvs)){
            QueryBuilder queryBuilder=new QueryBuilder(cla);
            boolean addedWhere=false;
            for (Map.Entry<String, Object> entry : kvs.entrySet()) {
                entry.getKey();
                entry.getValue();
                if (!addedWhere) {
                    addedWhere=true;
                    queryBuilder.where(entry.getKey()+ "=?",entry.getValue());
                }else {
                    queryBuilder.whereAnd(entry.getKey()+ "=?",entry.getValue());
                }
            }
            List<T> results = getDB().<T>query(queryBuilder);
            if (UtilCollection.notEmpty(results)){
                return results.get(0);
            }
        }
        return null;
    }

    /**
     * 查询  某字段 等于 Value的值
     */
    public static <T> T querySingle(Class<T> cla, String field, String value) {
        List<T> results= getDB().<T>query(new QueryBuilder(cla).where(field + "=?", value));
        if (UtilCollection.notEmpty(results)){
            return results.get(0);
        }
        return null;
    }

    /**
     * 查询  某字段 等于 Value的值
     */
    public static <T> T querySingle(Class<T> cla, String field1, String value1,
                                    String field2, String value2) {
        List<T> results= getDB().<T>query(new QueryBuilder(cla)
                .where(field1 + "=?", value1)
                .whereAnd(field2 + "=?", value2)
        );
        if (UtilCollection.notEmpty(results)){
            return results.get(0);
        }
        return null;
    }

    public static <T> List<T> queryDistinct(Class<T> cla, String field) {
        return getDB().query(
                new QueryBuilder<T>(cla).columns(new String[]{field}).distinct(true));
    }

    //可以覆盖bean里面注解的冲突算法
    public static <T> long insertReplace(T t) {
        return getDB().insert(t, ConflictAlgorithm.Replace);
    }

    //可以覆盖bean里面注解的冲突算法
    public static <T> long insertReplace(List<T> list) {
        return getDB().insert(list, ConflictAlgorithm.Replace);
    }

    //可以覆盖bean里面注解的冲突算法
    public static <T> void insertIgnore(T t) {
        getDB().insert(t, ConflictAlgorithm.Ignore);
    }

    //可以覆盖bean里面注解的冲突算法
    public static <T> void insertIgnore(List<T> list) {
        getDB().insert(list, ConflictAlgorithm.Ignore);
    }

//    public static void encrypt(Context ctxt, String dbName,
//                               String passphrase) throws Exception {
//        if (SPUtils.getInstance().getBoolean("encrypt",true)) {
//            SPUtils.getInstance().put("encrypt",false);
//            // 获取数据库路径
//            File originalFile = ctxt.getDatabasePath(dbName);
//
//            if (originalFile.exists()) { // 判断数据库.是否存在
//                Log.i("SSS", "db yes");
//            } else {
//                Log.i("SSS", "db no");
//            }
//
//            if (originalFile.exists()) {
//                File newFile = File.createTempFile("sqlcipherutils", "tmp",
//                        ctxt.getCacheDir());
//                SQLiteDatabase.loadLibs(ctxt);
//                SQLiteDatabase db = SQLiteDatabase.openDatabase(originalFile.getAbsolutePath(),
//                        "", null,
//                        SQLiteDatabase.OPEN_READWRITE);
//
//                db.rawExecSQL(String.format("ATTACH DATABASE '%s' AS encrypted KEY '%s';",
//                        newFile.getAbsolutePath(), passphrase));
//                db.rawExecSQL("SELECT sqlcipher_export('encrypted')");
//                db.rawExecSQL("DETACH DATABASE encrypted;");
//
//                int version = db.getVersion();
//
//                db.close();
//
//                db = SQLiteDatabase.openDatabase(newFile.getAbsolutePath(),
//                        passphrase, null,
//                        SQLiteDatabase.OPEN_READWRITE);
//                db.setVersion(version);
//                db.close();
//
//                originalFile.delete();
//                newFile.renameTo(originalFile);
//            }
//        }
//    }

    public static void deleteDB(){
        getDB().deleteDatabase();
        mLiteOrm=null;
    }

    public static void close(){
        getDB().close();
        mLiteOrm=null;
    }
}
