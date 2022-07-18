package cn.cappuccinoj.dianping.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.opencsv.CSVReader;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @Author cappuccino
 * @Date 2022-05-25 19:19
 */
@Controller("/es")
@RequestMapping("/es")
public class ESController {

    @Resource
    private TransportClient transportClient;

    @RequestMapping("/get")
    @ResponseBody
    public ResponseEntity get(@RequestParam(name = "id") Integer id) {
        GetResponse getResponse = transportClient.prepareGet("movie", null, id.toString()).get();
        return new ResponseEntity(getResponse.getSource(), HttpStatus.OK);
    }

    public static void main(String[] args) {
        String str = "1,2,3,4,,5";
        String[] a = str.split(",");
        int i = 0;
    }

    @RequestMapping("/importdata")
    @ResponseBody
    public ResponseEntity importData() throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        int lineId = 0;

        InputStreamReader in = new InputStreamReader(new FileInputStream("./tmdb_5000_movies.csv"), StandardCharsets.UTF_8);
        CSVReader reader = new CSVReader(in, ',');
        List<String[]> allRecords = reader.readAll();
        for (String[] records : allRecords) {
            lineId++;
            if (lineId == 1) {
                continue;
            }
            try {
                JSONArray castJsonArray = JSONArray.parseArray(records[20]);
                String character = (String) castJsonArray.getJSONObject(0).get("character");
                System.out.println("character = " + character);
                String name = (String) castJsonArray.getJSONObject(0).get("name");
                JSONObject cast = new JSONObject();
                cast.put("character", character);
                cast.put("name", name);
                String date = records[11];
                if (StringUtils.isBlank(date)) {
                    date = "1970/01/01";
                }
                bulkRequest.add(new IndexRequest("movie", "_doc", String.valueOf(lineId - 1)).source(XContentType.JSON,
                        "title", records[17],
                        "tagline", records[16],
                        "release_date", date,
                        "popularity", records[8],
                        "cast", cast,
                        "overview", records[7]));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        reader.close();

        transportClient.bulk(bulkRequest, new ActionListener<BulkResponse>() {
            @Override
            public void onResponse(BulkResponse bulkItemResponses) {
                //int i = 0;
                //for (BulkItemResponse res : bulkItemResponses.getItems()) {
                //    i++;
                //    if (res.getResponse() == null) {
                //        System.out.println("line=" + i + ",res=" + res.getFailureMessage());
                //    } else {
                //        System.out.println("line=" + i + ",res=" + res.getResponse().toString());
                //    }
                //}

                System.out.println("bulkItemResponses = " + bulkItemResponses);
            }

            @Override
            public void onFailure(Exception e) {
                System.out.println("e = " + e);
            }

        });

        //File csv = new File("./tmdb_5000_movies.csv");//CSV文件路径
        //BufferedReader br = null;
        //
        //try {
        //    br = new BufferedReader(new FileReader(csv));
        //} catch (FileNotFoundException e) {
        //    e.printStackTrace();
        //}
        //
        //String line = "";
        //String everyLine = "";
        //
        //BulkRequest bulkRequest1 = new BulkRequest();
        //try {
        //    List<String> allString = new ArrayList<>();
        //    int lineId = 0;
        //    while ((line = br.readLine()) != null) //读取到的内容给line变量
        //    {
        //        lineId++;
        //        everyLine = line;
        //        String[] lineContent = everyLine.split(",");
        //
        //        bulkRequest.add(new IndexRequest("movie", "_doc", String.valueOf(lineId)).source(XContentType.JSON,
        //                "title", lineContent[17],
        //                "tagline", lineContent[16]));
        //
        //        allString.add(everyLine);
        //    }
        //    System.out.println("csv表格中所有行数 :" + allString.size());
        //    transportClient.bulk(bulkRequest, new ActionListener<BulkResponse>() {
        //        @Override
        //        public void onResponse(BulkResponse bulkItemResponses) {
        //            System.out.println("bulkItemResponses = " + bulkItemResponses);
        //        }
        //
        //        @Override
        //        public void onFailure(Exception e) {
        //            System.out.println("e = " + e);
        //        }
        //
        //    });
        //} catch (Exception e) {
        //    e.printStackTrace();
        //}

        return new ResponseEntity("", HttpStatus.OK);
    }
}
