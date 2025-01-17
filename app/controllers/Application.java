package controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.databind.JsonNode;

import models.Scan;
import play.*;
import play.mvc.*;
import play.data.Form;
import play.libs.Json;
import views.html.*;
import views.html.defaultpages.error;

public class Application extends Controller {
	final static Form<Scan> myForm = Form.form(Scan.class);
	
	public static Result wait_job(String jobid) throws InterruptedException {
		return ok(wait.render(jobid));
    }
	
	public static Result help() {
        return ok(help.render());
    }
	
	public static Result about() {
        return ok(about.render());
    }
	public static Result download() {
        return ok(download.render());
    }
	
    public static Result index() {
        return ok(index.render());
    }
    public static Result scan() {
    	Form<Scan> newform = myForm.fill(new Scan());
        return ok(scan.render(newform));
    }
    
    public static Result getFile(String job_id, String file, String dlname){
    	File f = new File(jobPath(job_id) + file);
    	response().setContentType("application/x-download");  
    	response().setHeader("Content-disposition","attachment; filename="+dlname); 
    	return ok(f);
    }
    
    public static Result getFlatFile(String file, String dlname){
    	File f = new File("public/R/generate_data/" + file);
    	response().setContentType("application/x-download");  
    	response().setHeader("Content-disposition","attachment; filename="+dlname); 
    	return ok(f);
    }
    
    public static Result show_job(String jobid) throws IOException{
    	
    	Scan job = getJobFromJsonFile(jobid);
    	if(job == null) return badRequest(oops.render("jobNotFound"));
    	
    	if(job.has_error){
        	return badRequest(oops.render("error"));
        }
    	
    	String no_data = jobPath(jobid) + "no_data";
    	if(new File(no_data).exists()){
        	Form<Scan> filledForm = myForm.fill(job);
    		filledForm.reject("job_id", "Sorry, no rewired kinases were identified.");
    		return badRequest(scan.render(filledForm));
    	}

        if(!job.mut_error.isEmpty()){
        	Form<Scan> filledForm = myForm.fill(job);
        	filledForm.reject("mut_data", "Reference amino acid for mutation did not match the sequence! " + job.mut_error.get(0));
        	return badRequest(scan.render(filledForm));
        }

        if(!job.ps_error.isEmpty()){
        	Form<Scan> filledForm = myForm.fill(job);
        	filledForm.reject("ps_data", "Phosphorylation site position does not match! " + job.ps_error.get(0));
        	return badRequest(scan.render(filledForm));
        }
        
        

        try{
        	job.html = Help.readFile(Application.jobPath(job.job_id) + "html.txt");
    	}catch(Exception e){
    		Logger.info("XXXXXX");
    		Writer writer = new StringWriter();
    		PrintWriter printWriter = new PrintWriter(writer);
    		e.printStackTrace(printWriter);
    		String s = writer.toString();
    		
    		Logger.error(s);
    		return badRequest(oops.render("error"));
    	}
    	
    	return ok(scandone.render(job));
    }

    public static Result submit() throws IOException{
    	try{
    		// Get form
            Form<Scan> filledForm = myForm.bindFromRequest();
            Scan job = filledForm.get();
            
            if(job.fasta_data.isEmpty() | job.mut_data.isEmpty())
            	return badRequest(oops.render("memory"));
            
            if(job.ps_data.isEmpty() && job.incl_ps.equals("yes"))
            	return badRequest(oops.render("memory"));
            
            
            //Help.writeFile(job.fasta_data, "/Users/omarwagih/Desktop/play_fa.txt");
            Logger.info("FASTA = "+ job.fasta_data);
            
            Logger.info("JOB HAS ID = "+ job.job_id);
            Logger.info("MODEL DATA = "+ job.model_data);
            Logger.info("CENT = "+ job.incl_cent);
        	//job.job_id = 
            String jobid = UUID.randomUUID().toString();
            if(job.job_id != ""){
            	jobid = job.job_id;
            	 // Delete previous json file
            	 String path = jobPath(job.job_id) +job.job_id+".json";
            	 boolean delsuc = new File(path).delete();

                 // Delete no_data file if it exists
                 String no_data = jobPath(jobid) + "no_data";
                 boolean delno = new File(no_data).delete();
            }else{
            	job.job_id = jobid;
            }

            new Thread(job). start( );
            return redirect("/wait/"+jobid);
            
    	}catch(Exception e){
    		Logger.info("YYYYYY");
    		Writer writer = new StringWriter();
    		PrintWriter printWriter = new PrintWriter(writer);
    		e.printStackTrace(printWriter);
    		String s = writer.toString();
    		
    		Logger.error(s);
    		
    		return badRequest(oops.render("error"));
    	}
        // Logger.info(job.toString());
        

    	
    }
    
    public static Scan getJobFromJsonFile(String jobid) {
        try {
            String jsonString = Help.readFile( jobPath(jobid) + jobid+".json" );
            Scan job = Json.fromJson(Json.parse(jsonString), Scan.class);
            
            return job;
        } catch (Exception e) {
            Logger.error(e.getMessage());
            return null;
        }
    }

    public static boolean writeJsonJobToFile(Scan job) {
        try {
            JsonNode jn = Json.toJson(job);
            String path = jobPath(job.job_id) +job.job_id+".json";
            Logger.info("Writing job to: "+path);
            Help.writeFile(jn.toString(), path);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public static String jobPath(String jobid){
    	return "public/jobs/" +jobid + "/";
    }
}

