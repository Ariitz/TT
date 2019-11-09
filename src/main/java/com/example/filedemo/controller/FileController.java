package com.example.filedemo.controller;

import com.example.filedemo.payload.UploadFileResponse;
import com.example.filedemo.service.FileStorageService;
import com.mongodb.*;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


import java.io.IOException;
import java.net.UnknownHostException;

import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;








@RestController
public class FileController {

    public String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);


    @PostMapping("/uploadFile")
    public UploadFileResponse uploadFile(@RequestParam("file") MultipartFile file,
                                         @RequestParam("correo") String correo
            //,
              //                           @RequestParam("audio") String audiog
    ) {
        System.out.println("Entro aca");
        String fileName = "";



        System.out.println(file.getName());
        System.out.println(file.getContentType());
        try {

            System.out.println(file.getBytes().length);
        }catch (Exception e){
            e.printStackTrace();
        }



        //SAVING FILE


        try{

            Mongo mongo = new MongoClient("localhost", 27017);
            DB db = mongo.getDB("sgplat");

            DBCollection collection = db.getCollection("intento");

            GridFS audio = new GridFS(db, "audio");
            GridFSInputFile gfsFile = audio.createFile(file.getBytes());

            gfsFile.setFilename("idRecibido"+new Date().getTime()+".mp3");


            System.out.println("Antes " + gfsFile.getId());

            // save the image file into mongoDB
            gfsFile.save();

            System.out.println("Despues " +gfsFile.getId());

            String respuesta=WSConsumer.get("http://localhost:8080/rest/intento/up?edo=no%20realizada&corPac="+correo+"&nomPru=palabras&fechaI=8/11/2019"+
                    "&contI="+gfsFile.getId()+"&tiempo=3"
                    //+audiog
            );
            System.out.print(respuesta);

            GridFSDBFile gridFSDBFile = audio.findOne(gfsFile.getFilename());
            System.out.print(gridFSDBFile);
            gridFSDBFile.writeTo("/home/daniel/"+gfsFile.getFilename());


        }
        catch (Exception e){
            e.printStackTrace();
        }











        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/downloadFile/")
                .path(fileName)
                .toUriString();

        return new UploadFileResponse(fileName, fileDownloadUri,
                file.getContentType(), file.getSize());
    }


    @GetMapping("/downloadFile/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        // Load file as Resource
        Resource resource = null;
        try {
            resource = new UrlResource("");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            logger.info("Could not determine file type.");
        }

        // Fallback to the default content type if type could not be determined
        if(contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }




    public String fn;

    public String getFn() {
        return fn;
    }

    public void setFn(String fn) {
        this.fn = fn;
    }


    @GetMapping("/download/{intento}/")
    public void audio(@PathVariable("intento") String filename) throws IOException {


        //Mongo mongo = new MongoClient("localhost", 27017);
        //DB db = mongo.getDB("sgplat");

        MongoClient mongoClient = new MongoClient("127.0.0.1", 27017);
        DB db = mongoClient.getDB("sgplat");
        MongoDatabase mongoDatabase = mongoClient.getDatabase("sgplat");

        GridFS audio = new GridFS(db, "audio");
        GridFSDBFile gridFSDBFile = audio.findOne(filename);
        System.out.print(gridFSDBFile);
        gridFSDBFile.writeTo("/home/daniel/"+filename);

        GridFSBucket gridFSBucket = GridFSBuckets.create(mongoDatabase);

        GridFSDownloadStream downloadStream = gridFSBucket.openDownloadStream(filename);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        int fileLength = (int) downloadStream.getGridFSFile().getLength();
        byte[] bytesToWriteTo = new byte[fileLength];
        int streamDownloadPosition = 0;
        while(streamDownloadPosition != -1) {
            streamDownloadPosition += downloadStream.read(bytesToWriteTo, streamDownloadPosition, fileLength);
        }
        downloadStream.close();

    }


    /*
    @RequestMapping(path = "/download", method = RequestMethod.GET)
    public ResponseEntity<Resource> download(String param) throws IOException {


        Mongo mongo = new MongoClient("localhost", 27017);
        DB db = mongo.getDB("sgplat");

        MongoClient mongoClient = new MongoClient("127.0.0.1", 27017);
        MongoDatabase mongoDatabase = mongoClient.getDatabase("sgplat");


        GridFS audio = new GridFS(db, "audio");
        GridFSDBFile gridFSDBFile = audio.findOne(param);

        GridFSBucket gridFSBucket = GridFSBuckets.create(mongoDatabase);
        System.out.print(gridFSDBFile);
        gridFSDBFile.writeTo("/home/daniel/"+param);


        ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(resource);
    }
*/




}
