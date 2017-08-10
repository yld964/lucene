package lucence;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Constants;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by lidongyang on 2017/8/10 0010.
 */
public class IndexTest {
    public static void main(String[] args) {
        //创建索引
        createindex();
        //搜索
        doquery();
    }

    /**
     * 建立索引，大概的过程为
     *
     * 先将document传递给分词器进行分词，在分词之后，通过索引写入工具将索引写入到指定目录
     */
    public static void createindex(){
        try {
            //建立索引的目录，这里是将索引建立到e盘下的index文件夹中
            Directory directory = FSDirectory.open(new File("e:/index").toPath());
            //StandardAnalyzer是lucene自带的一个分词器
            Analyzer analyzer = new StandardAnalyzer();
            //建立lucene的基本单位-文档，类比为数据库的一条记录
            Document document = new Document();
            //在文档中添加域-field，类比为数据库中一条记录的一个字段，第一个参数为字段名，第二个参数为字段值
            document.add(new Field("name","his is the text to be indexed.", TextField.TYPE_STORED));
            document.add(new Field("name","hello world.", TextField.TYPE_STORED));
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            //创建索引写入工具
            IndexWriter writer = new IndexWriter(directory,config);
            //将索引写入制定的目录
            writer.addDocument(document);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据索引查询，大概的过程为
     * 首先创建查询的Query，通过indexSearcher进行查询，得到命中的Topdocs，通过Topdocs的scoreDoces属性，拿到ScoreDoc，
     * 通过ScoreDoc，得到对应的文档编号，IndexSearcher通过文档编号，使用IndexReader对指定索引下的索引内容进行读取，得到命中后的文档后返回。
     */
    public static void doquery(){
        String querystr = "his";
        String[] fields = {"name"};
        try {
            //建立索引目录，
            Directory directory = FSDirectory.open(new File("e:/index").toPath());
            //创建读目录工具Reader
            DirectoryReader directoryReader = DirectoryReader.open(directory);
            //分词器
            Analyzer analyzer = new StandardAnalyzer();
            //创建查询内容分析器
            QueryParser queryParser = new MultiFieldQueryParser(fields,analyzer);
            //创建查询Query
            Query query = queryParser.parse(querystr);
            //创建IndexSearcher
            IndexSearcher indexSearcher = new IndexSearcher(directoryReader);

            //==============以下内容为 高亮显示 代码 ===============

            Formatter formatter = new SimpleHTMLFormatter("<font color='red'>","</font>");
            Scorer scorer = new QueryScorer(query);
            //参数30截取前30个字符进行显示
            Fragmenter fragmenter = new SimpleFragmenter(30);
            Highlighter highlighter = new Highlighter(formatter,scorer);
            highlighter.setTextFragmenter(fragmenter);

            //==============以上内容为 高亮显示 代码 ===============

            //IndexSearcher通过query得到命中的topDocs，第一个参数是Query，第二个参数是显示符合条件的条数，这里表示显示2条
            TopDocs topDocs = indexSearcher.search(query,2);
            System.out.println("符合条件的文档总数" + topDocs.totalHits);
            for (int i= 0;i<topDocs.totalHits;i++){
                //通过topdocs的属性得到scoredoc,并获取到doc编号，然后indexsearcher通过这个编号找到文档，就可以了
                Document doc = indexSearcher.doc(topDocs.scoreDocs[i].doc);
                System.out.println(doc.get("name"));
                //这个代码是高亮显示的
                System.out.println(highlighter.getBestFragment(analyzer,"name",doc.get("name")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
