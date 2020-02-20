package main;

    import java.io.File;
    import java.io.InputStream;
    import java.nio.file.Files;
    import java.nio.file.Path;
    import java.nio.file.Paths;
    import java.util.ArrayList;
    import java.util.HashMap;
    import static spark.Spark.*;
    import com.google.gson.Gson;
    import models.*;
    import spark.ModelAndView;
    import spark.template.handlebars.HandlebarsTemplateEngine;

    import javax.servlet.MultipartConfigElement;
    import javax.servlet.http.Part;

public class Launcher {
     public static void main(String[] args) {
         staticFiles.location("/public");
         staticFiles.externalLocation("slike");
         port(5000);
         String path= "artikli.json";
         HashMap<String,Object> polja=new HashMap<>();

         get("/",(request, response) -> {
             polja.put("artikli",Data.readFromJson(path));
             return new ModelAndView(polja,"index.hbs");
         }, new HandlebarsTemplateEngine());

         get("/pretraga",(request, response) -> {
             polja.put("kriterijum",new String[]{"naziv","kategorija","cena","zalihe"});
             return new ModelAndView(polja,"pretraga.hbs");
         }, new HandlebarsTemplateEngine());

         path("/pretraga",() -> {
             post("/podaci",(request, response) -> {
                 response.type("application/json");
                 String kriterijum=request.queryParams("kriterijum");
                 String podaci=request.queryParams("podaci");
                 boolean tacan= Boolean.parseBoolean(request.queryParams("tacan"));
                 Gson g= new Gson();
                 switch (kriterijum) {
                     case "naziv":
                         if(tacan)
                             return g.toJson(Data.readFromJson(path).stream().filter(x -> x.getNaziv().toLowerCase().equals(podaci.toLowerCase())).toArray());
                         else
                             return g.toJson(Data.readFromJson(path).stream().filter(x -> x.getNaziv().toLowerCase().startsWith(podaci.toLowerCase())).toArray());
                     case "kategorija":
                         if(tacan)
                             return g.toJson(Data.readFromJson(path).stream().filter(x -> x.getKategorija().toLowerCase().equals(podaci.toLowerCase())).toArray());
                         else
                             return g.toJson(Data.readFromJson(path).stream().filter(x -> x.getKategorija().toLowerCase().startsWith(podaci.toLowerCase())).toArray());
                     default:
                         return g.toJson(null);
                 }
             });

             post("/opseg",(request, response) -> {
                 response.type("application/json");
                 double odBr=Double.parseDouble(request.queryParams("od"));
                 double doBr=Double.parseDouble(request.queryParams("do"));
                 String kriterijum=request.queryParams("kriterijum");
                 boolean tacan= Boolean.parseBoolean(request.queryParams("tacan"));
                 Gson g= new Gson();
                 switch (kriterijum) {
                     case "cena":
                         if(tacan)
                             return g.toJson(Data.readFromJson(path).stream().filter(x -> x.getCena() == doBr).toArray());
                         else
                             return g.toJson(Data.readFromJson(path).stream().filter(x -> x.getCena() >= odBr && x.getCena() <= doBr).toArray());
                     case "zalihe":
                         if(tacan)
                             return g.toJson(Data.readFromJson(path).stream().filter(x -> x.getZalihe() == doBr).toArray());
                         else
                             return g.toJson(Data.readFromJson(path).stream().filter(x -> x.getZalihe() >= odBr && x.getZalihe() <= doBr).toArray());
                     default:
                         return g.toJson(null);
                 }
             });
         });

         get("/dodavanje",(request, response) -> {
             polja.put("poruka",null);
             return new ModelAndView(polja,"dodavanje.hbs");
         },new HandlebarsTemplateEngine());

         post("/kreirajArtikal", "multipart/form-data",(request, response) -> {
             ArrayList<Artikal> artikli=Data.readFromJson(path);
             String location = "slike";          // the directory location where files will be stored
             long maxFileSize = 100000000;       // the maximum size allowed for uploaded files
             long maxRequestSize = 100000000;    // the maximum size allowed for multipart/form-data requests
             int fileSizeThreshold = 1024;       // the size threshold after which files will be written to disk

             MultipartConfigElement multipartConfigElement = new MultipartConfigElement(location, maxFileSize, maxRequestSize, fileSizeThreshold);
             request.raw().setAttribute("org.eclipse.jetty.multipartConfig", multipartConfigElement);

             String naziv=request.queryParams("naziv");
             String kategorija=request.queryParams("kategorija");
             double cena=Double.parseDouble(request.queryParams("cena"));
             int zalihe=Integer.parseInt(request.queryParams("zalihe"));

             Part slika=request.raw().getPart("slika");
             int id=Artikal.noviID(artikli);
             String fajl;

             if(slika.getSize()>0) {
                 fajl=id+slika.getContentType().replace("image/",".");
                 Path out = Paths.get("slike/" + fajl);
                 try (final InputStream in = slika.getInputStream()) {
                     Files.copy(in, out);
                     slika.delete();
                 } catch (Exception ex) {
                     System.out.println(ex.getMessage());
                     fajl="default.jpg";
                 }
             }
             else
                 fajl="default.jpg";

             Artikal art=new Artikal(id,fajl,kategorija,naziv,cena,zalihe);
             artikli.add(art);
             Data.writeToJSON(artikli,path);
             polja.put("poruka","Uspesno ste uneli novi artikal!");

             return new ModelAndView(polja,"dodavanje.hbs");
         }, new HandlebarsTemplateEngine());

         get("/izmena",(request, response) -> {
             polja.put("artikli",Data.readFromJson(path));
             return new ModelAndView(polja,"izmena.hbs");
         }, new HandlebarsTemplateEngine());

         path("/izmena",() -> {
             post("/obrisi",(request, response) -> {
                 response.type("application/json");
                 int id=Integer.parseInt(request.queryParams("id"));
                 File slika;
                 Gson g= new Gson();
                 ArrayList<Artikal> artikli=Data.readFromJson(path);
                 for (Artikal art:artikli) {
                     if(art.getId_artikla()==id) {
                         if(!art.getPath().equals("default.jpg")) {
                             slika = new File("slike/" + art.getPath());
                             if (slika.exists())
                                 slika.delete();
                         }
                         artikli.remove(art);
                         break;
                     }
                 }
                 Data.writeToJSON(artikli,path);

                 return g.toJson(artikli);
             });

             post("/pronadji",(request, response) -> {
                 response.type("application/json");
                 int id=Integer.parseInt(request.queryParams("id"));
                 Gson g= new Gson();
                 ArrayList<Artikal> artikli=Data.readFromJson(path);
                 for (Artikal art:artikli) {
                     if(art.getId_artikla()==id) {
                         return g.toJson(art);
                     }
                 }
                 return g.toJson(null);
             });

             post("/izmeni", "multipart/form-data",(request, response) -> {
                 ArrayList<Artikal> artikli=Data.readFromJson(path);
                 String location = "slike";          // the directory location where files will be stored
                 long maxFileSize = 100000000;       // the maximum size allowed for uploaded files
                 long maxRequestSize = 100000000;    // the maximum size allowed for multipart/form-data requests
                 int fileSizeThreshold = 1024;       // the size threshold after which files will be written to disk

                 MultipartConfigElement multipartConfigElement = new MultipartConfigElement(location, maxFileSize, maxRequestSize, fileSizeThreshold);
                 request.raw().setAttribute("org.eclipse.jetty.multipartConfig", multipartConfigElement);

                 String naziv=request.queryParams("naziv");
                 String kategorija=request.queryParams("kategorija");
                 double cena=Double.parseDouble(request.queryParams("cena"));
                 int zalihe=Integer.parseInt(request.queryParams("zalihe"));

                 Part slika=request.raw().getPart("slika");
                 int id=Integer.parseInt(request.queryParams("id"));
                 String fajl;


                 for (Artikal art:artikli) {
                     if(art.getId_artikla()==id) {
                         if(slika.getSize()>0) {
                             if(!art.getPath().equals("default.jpg")) {
                                 File sli = new File("slike/" + art.getPath());
                                 if (sli.exists())
                                     sli.delete();
                             }
                             fajl=id+slika.getContentType().replace("image/",".");
                             Path out = Paths.get("slike/" + fajl);
                             try (final InputStream in = slika.getInputStream()) {
                                 Files.copy(in, out);
                                 slika.delete();
                             } catch (Exception ex) {
                                 System.out.println(ex.getMessage());
                                 fajl="default.jpg";
                             }
                         }
                         else
                             fajl=art.getPath();
                         art.setPath(fajl);
                         art.setNaziv(naziv);
                         art.setKategorija(kategorija);
                         art.setCena(cena);
                         art.setZalihe(zalihe);
                         break;
                     }
                 }
                 Data.writeToJSON(artikli,path);
                 polja.put("artikli",artikli);
                 return new ModelAndView(polja,"izmena.hbs");
             }, new HandlebarsTemplateEngine());
         });
     }
 }
