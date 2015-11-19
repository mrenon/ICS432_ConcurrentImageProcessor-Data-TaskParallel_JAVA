/*
 * Mauricio Renon
 * ICS432
 * 
 * ConcurrentImageProcessor.java
 * 
 * a concurrent program to read images from a directory
 * process them with either a invert, smear, oil1, or oil6 filter
 * and write them back to disk
 * 
 *
 */

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.awt.image.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import javax.imageio.ImageIO;

import com.jhlabs.image.*;

public class ConcurrentImageProcessorDataParallel {
	static String filterType;
	static String directory;

	static int numThreads;
	static File folder;
	static List<File> listOfFiles;
	
	
	static long overallTime;
	static double loading = 0;
	
	
	

/*
 * creates LinkedBlockQueue to store the images of size 8
 * takes in two command line arguments and one optional command line argument
 * (optional) the number of threads
 * 1) the filter
 * 2) the working directory
 * 
 * creates threads to read, process, write
 * 
 * there is a thread for a progress bar display but it is commented out
 * 
 * @param args array of command line arguments
 * 	
 */
	public static void main(String[] args) throws InterruptedException{
		
		LinkedBlockingQueue<BufferedImage> readToProcess = new LinkedBlockingQueue<BufferedImage>(7); //because 0 counts as 1
		LinkedBlockingQueue<BufferedImage> processToWrite = new LinkedBlockingQueue<BufferedImage>(7); //because 0 counts as 1
		LinkedBlockingQueue<BufferedImage> progressBar = new LinkedBlockingQueue<BufferedImage>();
		
		if(args.length == 0){
			System.out.println("Usage: ConcurrentImageProcessor (optional # of Processor Threads) [oil1|oil6|smear|invert|weird] < directory >");
		}
		else if(args.length == 2){
			if(args[0].equalsIgnoreCase("oil1") ||
				args[0].equalsIgnoreCase("oil6") ||
				args[0].equalsIgnoreCase("smear") ||
				args[0].equalsIgnoreCase("invert")||
				args[0].equalsIgnoreCase("weird")){
				
					numThreads = 1;
					filterType = args[0];
					directory = args[1];		
			}
			else{
				System.out.println("Usage: ConcurrentImageProcessor (optional # of Processor Threads) [oil1|oil6|smear|invert|weird] < directory >");
			}
				
			
		}
		else{
			numThreads = Integer.parseInt(args[0]);
			filterType = args[1];
			directory = args[2];
		}
			
			
			   Path path = Paths.get(directory);
			   listOfFiles = new ArrayList<File>();

			   try (DirectoryStream<Path> ds = Files.newDirectoryStream(path, "image_*.jpg")) {
			     for (Path f : ds) {
			       listOfFiles.add(f.toFile());
			     }
			   } catch (IOException e) {
			     System.err.println(e);
			   }


			overallTime = 0;
			
			FileReadImageData FRI = new FileReadImageData(readToProcess, directory, listOfFiles);
			Thread readThread = new Thread(FRI);
			
			ProcessImageData PI = new ProcessImageData(readToProcess, processToWrite, filterType, readThread, numThreads);
			Thread processThread = new Thread(PI);
			
			FileWriteImageData FWI = new FileWriteImageData(processToWrite, listOfFiles, directory, filterType, processThread, progressBar);
			Thread writeThread = new Thread(FWI);
			

			
			overallTime = System.currentTimeMillis();
			
			readThread.start();
			processThread.start();
			writeThread.start();

			
			readThread.join();
			processThread.join();
			writeThread.join();	
			
			overallTime = System.currentTimeMillis() - overallTime;

			System.out.println();
			System.out.println("Overall Execution Time: " + (overallTime/1000.00) + " sec.");
	}
}

/**
 * Thread class FileReadImageData Class
 * Reads images within a directory and 
 * stores them in a queue of size 8, blocks when 
 * the queue is full and waits for space to open
 * @author mauriciofloydianrenon
 *
 */

class FileReadImageData implements Runnable{
	
	private final LinkedBlockingQueue<BufferedImage> readToProcess;
	private String directory;
	private List<File> listOfFiles;
	private static long ReadTimeStart;
	private static long readTime;

	/*
	 * FileReadImageData Constructor
	 * creates a FileReadImage object
	 * 
	 * @param readToProcess to Blocking Queue that holds images that were read to be processed
	 * @param directory name of the given directory to find the files in
	 * @param listOfFiles the files stored in an array
	 * 
	 */
	public FileReadImageData(LinkedBlockingQueue<BufferedImage> readToProcess, String directory, List<File> listOfFiles2){
		this.readToProcess = readToProcess;
		this.directory = directory;
		this.listOfFiles = listOfFiles2;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			for(int i = 0; i < listOfFiles.size(); i++){
				if (listOfFiles.get(i).getName().equalsIgnoreCase(".DS_Store")) {
					/* this checks initial .DS_Store file in folder, 
					 * if not checked SOMETIMES gives null pointer 
					 * */
				}
				else if(listOfFiles.get(i).getName().endsWith(".jpg") && listOfFiles.get(i).getName().startsWith("image")){
						BufferedImage input;
						//start time
						ReadTimeStart = System.currentTimeMillis();
						input = ImageIO.read(new File(directory +"/"+ listOfFiles.get(i).getName()));
						ReadTimeStart = System.currentTimeMillis() - ReadTimeStart; 
						readTime = readTime + ReadTimeStart;
						//update time
						readToProcess.put(input);
						System.out.print("r");
					}
			}
			System.out.println("Time Spent Reading: " + (readTime / 1000.00) + " sec.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(InterruptedException ie){
			ie.printStackTrace();
		}
	}
	
	/*
	 * getReadTime gets the total time to read images
	 * 
	 * @return readTime the total time it took to read the images
	 */
	public long getReadTime(){
		return readTime;
	}
}

/**
 * Thread class ProcesImageData Class
 * Processes images using invert,
 * smear, oil1, oil6, weird filters
 * 
 * @author mauriciofloydianrenon
 *
 */
class ProcessImageData implements Runnable{
	
	private final LinkedBlockingQueue<BufferedImage> readToProcess;
	private final LinkedBlockingQueue<BufferedImage> processToWrite;
	private String filterType;
	private BufferedImageOp filter;
	private BufferedImage newImage;
	private Thread readThread;
	private static long ProcessTimeStart;
	private static long processTime;
	private int numThreads;


	/*
	 * ProcessImageData Constructor
	 * creates a ProcessImage object
	 * 
	 * @param readToProcess to Blocking Queue that holds images that were read to be processed
	 * @param processToWrite Blocking Queue that holds images that were process to be written to disk
	 * @param filterType name of the given filter to apply to images
	 * @param readThread the Thread used for the reader class
	 * @param numThreads the number of threads to work on an image
	 * 
	 */
	public ProcessImageData(LinkedBlockingQueue<BufferedImage> readToProcess, LinkedBlockingQueue<BufferedImage> processToWrite, String filterType, Thread readThread, int numThreads){
		this.readToProcess = readToProcess;
		this.processToWrite = processToWrite;
		this.filterType = filterType;
		this.readThread = readThread;
		this.numThreads = numThreads;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try{
			while(readThread.isAlive() || readToProcess.size() != 0){ //while the queue is not empty
				ProcessTimeStart = System.currentTimeMillis();
				BufferedImage originalImage = (BufferedImage) readToProcess.take();
				newImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), originalImage.getType());
				if(filterType.equalsIgnoreCase("oil1")){ //oil1
					filter = new OilFilter();
					((OilFilter)filter).setRange(1);
					filter.filter(originalImage, newImage);
					processToWrite.put(newImage);
					ProcessTimeStart = System.currentTimeMillis() - ProcessTimeStart;
					processTime = processTime + ProcessTimeStart;
					System.out.print("p");
				}
				else if(filterType.equalsIgnoreCase("oil6")){ //oil6
					filter = new OilFilter();
					((OilFilter)filter).setRange(6);
					filter.filter(originalImage, newImage);
					processToWrite.put(newImage);
					ProcessTimeStart = System.currentTimeMillis() - ProcessTimeStart;
					processTime = processTime + ProcessTimeStart;
					System.out.print("p");
				}
				else if(filterType.equalsIgnoreCase("smear")){ //smear
					filter = new SmearFilter();
					((SmearFilter)filter).setShape(0);
					filter.filter(originalImage, newImage);
					processToWrite.put(newImage);
					ProcessTimeStart = System.currentTimeMillis() - ProcessTimeStart;
					processTime = processTime + ProcessTimeStart;
					System.out.print("p");
				}
				else if(filterType.equalsIgnoreCase("invert")){ //invert
					filter = new InvertFilter();
					filter.filter(originalImage, newImage);
					processToWrite.put(newImage);
					ProcessTimeStart = System.currentTimeMillis() - ProcessTimeStart;
					processTime = processTime + ProcessTimeStart;
					System.out.print("p");
				}
				else if(filterType.equalsIgnoreCase("weird")){ //weird
					// Apply Weird filter
					filter = new DataParallelWeirdFilter(numThreads);
					filter.filter(originalImage, newImage);
					processToWrite.put(newImage);
					ProcessTimeStart = System.currentTimeMillis() - ProcessTimeStart;
					processTime = processTime + ProcessTimeStart;
					System.out.print("p");
				}
			}
			System.out.println("Time Spent Processing: " + (processTime / 1000.00) + " sec.");
		}catch(InterruptedException e){
			e.printStackTrace();
		}
	}
	
	/*
	 * getProcessTime gets the total time to process images
	 * 
	 * @return processTime the total time it took to process the images
	 */
	public long getProcessTime(){
		return processTime;
	}
}

/**
 * Thread class FileWriteImageData Class
 * takes images from a blocking queue
 * writes it back to the given directory
 * 
 * @author mauriciofloydianrenon
 *
 */
class FileWriteImageData implements Runnable{

	private final LinkedBlockingQueue<BufferedImage> processToWrite;
	private final LinkedBlockingQueue<BufferedImage> progressBar;
	private List<File> listOfFiles;
	private String directory;
	private String filterType;;
	private static long WriteTimeStart;
	private static long writeTime;
	private Thread processThread;
	

	/*
	 * FileWriteImageData Constructor
	 * creates a ProcessImage object
	 * 
	 * @param processToWrite Blocking Queue that holds images that were process to be written to disk
	 * @param listOfFiles the files stored in an array
	 * @param directory the working directory
	 * @param filterType name of the given filter to apply to images
	 * @param processThread the Thread used for the processer class
	 * @param progressBar Blocking Queue that holds images that were to update a progress bar display (currently unimplemented)
	 * 
	 */	
	public FileWriteImageData(LinkedBlockingQueue<BufferedImage> processToWrite, List<File> listOfFiles2, String directory, String filterType, Thread processThread, LinkedBlockingQueue<BufferedImage> progressBar){
		this.processToWrite = processToWrite;
		this.listOfFiles = listOfFiles2;
		this.directory = directory;
		this.filterType = filterType;
		this.processThread = processThread;
		this.progressBar = progressBar;
		
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		while(processThread.isAlive() || processToWrite.size() != 0){
			for(int i = 0; i < this.listOfFiles.size(); i++){
				BufferedImage finishImage = null;
				try {
					finishImage = (BufferedImage) processToWrite.take();
					progressBar.put(finishImage);
					WriteTimeStart = System.currentTimeMillis(); //start time
					ImageIO.write(finishImage, "jpg", new File(directory + filterType + "_" + listOfFiles.get(i).getName()));
					WriteTimeStart = System.currentTimeMillis() - WriteTimeStart;
					writeTime = writeTime + WriteTimeStart;
					System.out.print("w");
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}catch(IOException e){
					System.out.println("Cannot write file");
					System.exit(1);
				}
			}
		}
		System.out.println("Time Spent Writing: " + (writeTime / 1000.00) + " sec.");
	}
	
	/*
	 * getProcessTime gets the total time to process images
	 * 
	 * @return processTime the total time it took to process the images
	 */
	public long getWriteTime(){
		return writeTime;
	}
}



