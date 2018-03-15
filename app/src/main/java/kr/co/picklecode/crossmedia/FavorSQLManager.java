package kr.co.picklecode.crossmedia;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.List;
import java.util.Vector;

import bases.Constants;
import kr.co.picklecode.crossmedia.models.Article;

public class FavorSQLManager extends SQLiteOpenHelper {

    private static FavorSQLManager instance;

    private int id;
    private int type;
    private String title;
    private String content;
    private String repPath;
    private String imgPath;
    private String regDate;
    private String uptDate;

    private FavorSQLManager(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public static FavorSQLManager getInstance(Context context){
        if(instance == null) instance = new FavorSQLManager(context, Constants.DATABASE.DB_NAME, null, 1);
        return instance;
    }

    // DB를 새로 생성할 때 호출되는 함수
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS tbl_Favor (`id` INTEGER PRIMARY KEY, `type` INTEGER, `title` TEXT, `content` TEXT, `repPath` TEXT, `imgPath` TEXT, `regDate` TEXT,  `upt_date` TEXT);");
    }

    // DB 업그레이드를 위해 버전이 변경될 때 호출되는 함수
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void insert(Article article) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO tbl_Favor(`id`, `type`, `title`, `content`, `repPath`, `imgPath`, `regDate`, `upt_date`) " +
                "VALUES(" +
                "'" + article.getId() + "'," +
                "'" + article.getType() + "'," +
                "'" + article.getTitle() + "'," +
                "'" + article.getContent() + "'," +
                "'" + article.getRepPath() + "'," +
                "'" + article.getImgPath() + "'," +
                "'" + article.getRegDate() + "'," +
                "'" + article.getUptDate() + "'" +
                ");");
        db.close();
    }

    public void delete(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM tbl_Favor WHERE `id`='" + id + "';");
        db.close();
    }

    public List<Article> getResultOrderBy(String orderByStatement) {
        SQLiteDatabase db = getReadableDatabase();
        if(orderByStatement == null || orderByStatement.trim().equals("")){
            orderByStatement = "`id` DESC";
        }

        Cursor cursor = db.rawQuery("SELECT * FROM tbl_Favor ORDER BY " + orderByStatement, null);

        List<Article> articles = new Vector<>();

        while (cursor.moveToNext()) {
            final Article temp = new Article();
            temp.setId(cursor.getInt(0));
            temp.setType(cursor.getInt(1));
            temp.setTitle(cursor.getString(2));
            temp.setContent(cursor.getString(3));
            temp.setRepPath(cursor.getString(4));
            temp.setImgPath(cursor.getString(5));
            temp.setRegDate(cursor.getString(6));
            temp.setUptDate(cursor.getString(7));
            articles.add(temp);
        }

        return articles;
    }
}

