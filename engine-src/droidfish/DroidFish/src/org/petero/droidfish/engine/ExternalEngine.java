/*
    DroidFish - An Android chess program.
    Copyright (C) 2011-2012  Peter Österlund, peterosterlund2@gmail.com

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.petero.droidfish.engine;

import android.content.Context;
import android.text.TextUtils;
import org.petero.droidfish.EngineOptions;

import java.io.*;
import java.nio.channels.FileChannel;

/**
 * Engine running as a process started from an external resource.
 */
public class ExternalEngine extends UCIEngineBase {
	protected final Context context;

	private File engineFile;

	protected static final String INTERNAL_SF_PATH = "/internal_sf";
	private static final String ENGINE_EXE_PATH = "/engine.exe";

	private final Report report;
	private Process engineProc;
	private Thread startupThread;
	private Thread exitThread;
	private Thread stdInThread;
	private Thread stdErrThread;
	private LocalPipe inLines;
	private boolean startedOk;
	private boolean isRunning;

	public ExternalEngine(Context context, String engine, Report report) {
		this.context = context;
		this.report = report;
		//Log.d(MyFish.TAG, "engine " + engine);

		if (TextUtils.isEmpty(engine)) {
			engine = "testEngine.eng";
		}
		File engineDir = getEngineDir();
		engineFile = new File(engineDir, engine);
		engineProc = null;
		startupThread = null;
		exitThread = null;
		stdInThread = null;
		stdErrThread = null;
		inLines = new LocalPipe();
		startedOk = false;
		isRunning = false;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	protected void startProcess() {
		try {
			//Log.d(MyFish.TAG, "engineFile " + engineFile);
			//Log.d(MyFish.TAG, "exePath " + exePath);

			File engineDir = getEngineDir();
			File storedEngineFile = new File(engineDir, ENGINE_EXE_PATH);
			copyFile(engineFile, storedEngineFile);
			String exePath = storedEngineFile.getPath();
			chmod(exePath);
			ProcessBuilder pb = new ProcessBuilder(exePath);
			synchronized (EngineUtil.nativeLock) {
				engineProc = pb.start();
			}

			startupThread = new Thread(new Runnable() {
				@Override
				public void run() {
					//Log.d(MyFish.TAG, "START AFTER 10 sec");
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						return;
					}
					//Log.d(MyFish.TAG, "STARTED");
					//Log.d(MyFish.TAG, "CHECK startedOk " + startedOk);
					//Log.d(MyFish.TAG, "CHECK isRunning " + isRunning);
					//Log.d(MyFish.TAG, "CHECK isUCI " + isUCI);
					if (startedOk && isRunning && !isUCI)
						report.reportError("uci_protocol_error");
				}
			});
			startupThread.start();

			exitThread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Process ep = engineProc;
						if (ep != null)
							ep.waitFor();
						isRunning = false;
						if (!startedOk)
							report.reportError("failed_to_start_engine");
						else {
							report.reportError("engine_terminated");
						}
					} catch (InterruptedException e) {
					}
				}
			});
			exitThread.start();

			// Start a thread to read stdin
			stdInThread = new Thread(new Runnable() {
				@Override
				public void run() {
					Process ep = engineProc;
					//Log.d("FISHLOG", "STARTENGINE ep " + ep);
					if (ep == null)
						return;
					InputStream is = ep.getInputStream();
					InputStreamReader isr = new InputStreamReader(is);
					BufferedReader br = new BufferedReader(isr, 8192);
					String line;
					try {
						boolean first = true;
						//Log.d("FISHLOG", "STARTENGINE ep " + ep);
						//Log.d("FISHLOG", "STARTENGINE br " + br);
						//Log.d("FISHLOG", "STARTENGINE br.readLine() " + br.readLine());
						while ((line = br.readLine()) != null) {
							//Log.d("FISHLOG", "STARTENGINE ep " + ep);
							//Log.d("FISHLOG", "STARTENGINE Thread.currentThread().isInterrupted() " + Thread.currentThread().isInterrupted());
							if ((ep == null) || Thread.currentThread().isInterrupted())
								return;
							synchronized (inLines) {
								inLines.addLine(line);

								//Log.d("FISHLOG", "STARTENGINE first " + first);

								if (first) {
									startedOk = true;
									isRunning = true;
									first = false;
								}
							}
						}
					} catch (IOException e) {
					}
					inLines.close();
				}
			});
			//Log.d("FISHLOG", "STARTENGINE start");
			stdInThread.start();

			// Start a thread to ignore stderr
			stdErrThread = new Thread(new Runnable() {
				@Override
				public void run() {
					byte[] buffer = new byte[128];
					while (true) {
						Process ep = engineProc;
						if ((ep == null) || Thread.currentThread().isInterrupted())
							return;
						try {
							int len = ep.getErrorStream().read(buffer, 0, 1);
							if (len < 0)
								break;
						} catch (IOException e) {
							return;
						}
					}
				}
			});
			stdErrThread.start();
		} catch (IOException ex) {
			ex.printStackTrace();
			report.reportError(ex.getMessage());
		}
	}

	private int hashMB = -1;
	private String gaviotaTbPath = "";
	private boolean optionsInitialized = false;

	/**
	 * @inheritDoc
	 */
	@Override
	public void initOptions(EngineOptions engineOptions) {
		super.initOptions(engineOptions);
		hashMB = getHashMB(engineOptions.hashMB);
		setOption("Hash", hashMB);
		if (engineOptions.engineProbe) {
			gaviotaTbPath = engineOptions.gtbPath;
			setOption("GaviotaTbPath", engineOptions.gtbPath);
			setOption("GaviotaTbCache", 8);
		}
		optionsInitialized = true;
	}

	/**
	 * Reduce too large hash sizes.
	 */
	private int getHashMB(int hashMB) {
		if (hashMB > 16) {
			int maxMem = (int) (Runtime.getRuntime().maxMemory() / (1024 * 1024));
			if (maxMem < 16)
				maxMem = 16;
			if (hashMB > maxMem)
				hashMB = maxMem;
		}
		return hashMB;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public boolean optionsOk(EngineOptions engineOptions) {
		if (!optionsInitialized)
			return true;
		if (hashMB != getHashMB(engineOptions.hashMB))
			return false;
		if (haveOption("gaviotatbpath") && !gaviotaTbPath.equals(engineOptions.gtbPath))
			return false;
		return true;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void setStrength(int strength) {
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public String readLineFromEngine(int timeoutMillis) {
		String ret = inLines.readLine(timeoutMillis);
		if (ret == null)
			return null;
		if (ret.length() > 0) {
//            System.out.printf("Engine -> GUI: %s\n", ret);
		}
		return ret;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void writeLineToEngine(String data) {
//        System.out.printf("GUI -> Engine: %s\n", data);
		data += "\n";
		try {
			Process ep = engineProc;
			if (ep != null)
				ep.getOutputStream().write(data.getBytes());
		} catch (IOException e) {
		}
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void shutDown() {
		if (startupThread != null)
			startupThread.interrupt();
		if (exitThread != null)
			exitThread.interrupt();
		super.shutDown();
		if (engineProc != null)
			engineProc.destroy();
		engineProc = null;
		if (stdInThread != null)
			stdInThread.interrupt();
		if (stdErrThread != null)
			stdErrThread.interrupt();
	}

	protected void copyFile(File from, File to) throws IOException {
		File engineDir = getEngineDir();
		new File(engineDir + INTERNAL_SF_PATH).delete();
		if (to.exists() && (from.length() == to.length()) && (from.lastModified() == to.lastModified()))
			return;
		if (to.exists()) {
			to.delete();
		}
		to.createNewFile();

		if (!from.exists()) { // Added logic here to prevent FileNotFoundException
			from.createNewFile();
		}

		FileChannel inFC = null;
		FileChannel outFC = null;
		try {
			inFC = new FileInputStream(from).getChannel();
			outFC = new FileOutputStream(to).getChannel();
			long cnt = outFC.transferFrom(inFC, 0, inFC.size());
			if (cnt < inFC.size())
				throw new IOException("File copy failed");
		} finally {
			if (inFC != null) {
				try {
					inFC.close();
				} catch (IOException ex) {
				}
			}
			if (outFC != null) {
				try {
					outFC.close();
				} catch (IOException ex) {
				}
			}
			to.setLastModified(from.lastModified());
		}
	}

	protected File getEngineDir() {
		File engineDir;
			engineDir = context.getFilesDir();
		if (engineDir != null && !engineDir.exists()) {
			if (!engineDir.mkdirs()) {
//				throw new IOException("Can't use engineDir");
//				throw new IllegalStateException("can't use engineDir");
			}
		}
		return engineDir;
	}

	private void chmod(String exePath) throws IOException {
		if (!EngineUtil.chmod(exePath))
			throw new IOException("chmod failed");
	}
}
