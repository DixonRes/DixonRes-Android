package com.dixonres.app;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends Activity {

    private EditText etInput;
    private EditText etVars;
    private EditText etModulus;
    private Button btnRun;
    private Button btnClear;
    private Button btnTestGmp;
    private Button btnTestMpfr;
    private Button btnTestFlint;
    private Button btnCopy;
    private TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etInput = (EditText) findViewById(R.id.et_input);
        etVars = (EditText) findViewById(R.id.et_vars);
        etModulus = (EditText) findViewById(R.id.et_modulus);
        btnRun = (Button) findViewById(R.id.btn_run);
        btnClear = (Button) findViewById(R.id.btn_clear);
        btnTestGmp = (Button) findViewById(R.id.btn_test_gmp);
        btnTestMpfr = (Button) findViewById(R.id.btn_test_mpfr);
        btnTestFlint = (Button) findViewById(R.id.btn_test_flint);
        btnCopy = (Button) findViewById(R.id.btn_copy);
        tvResult = (TextView) findViewById(R.id.tv_result);

        // Set default values for testing (very simple case)
        etInput.setText("x+y, x*y");
        etVars.setText("x");
        etModulus.setText("101");

        btnRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runDixon();
            }
        });

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearInputs();
            }
        });

        btnTestGmp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runTestProgram("test-gmp");
            }
        });

        btnTestMpfr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runTestProgram("test-mpfr");
            }
        });

        btnTestFlint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runTestProgram("test-flint");
            }
        });

        btnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyOutput();
            }
        });

        // Copy dixon executable to app data directory on first run
        copyDixonExecutable();
    }

    private void copyFileFromAssets(String fileName) {
        try {
            File targetFile = new File(getFilesDir(), fileName);
            // Always copy, regardless of whether the file exists already
            java.io.InputStream inputStream = getAssets().open(fileName);
            java.io.FileOutputStream outputStream = new java.io.FileOutputStream(targetFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            inputStream.close();
            outputStream.close();
            targetFile.setExecutable(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void copyDixonExecutable() {
        try {
            // Copy libraries from jniLibs to app data directory
            copyLibraries();
            
            // Copy dixon executable from assets to app data directory
            // Always copy to ensure we have the latest version
            File dixonFile = new File(getFilesDir(), "dixon");
            tvResult.setText("Copying dixon file from assets...");
            copyFileFromAssets("dixon");
            tvResult.setText("Dixon file copied and made executable");
            
            // Copy test programs
            copyFileFromAssets("test-gmp");
            copyFileFromAssets("test-mpfr");
            copyFileFromAssets("test-flint");
            
        } catch (Exception e) {
            e.printStackTrace();
            tvResult.setText("Error copying dixon: " + e.getMessage());
            Toast.makeText(this, "Failed to copy dixon executable: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void copyOutput() {
        try {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Dixon Output", tvResult.getText().toString());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Output copied to clipboard!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to copy output: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void readStream(InputStream stream, final StringBuilder resultText, final String prefix) {
        try {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = stream.read(buffer)) != -1) {
                final String chunk = new String(buffer, 0, bytesRead);
                resultText.append(chunk);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvResult.setText(resultText.toString());
                    }
                });
            }
        } catch (Exception e) {
            // Ignore
        }
    }
    
    private void runTestProgram(final String programName) {
        final StringBuilder resultText = new StringBuilder();
        resultText.append("=== Starting " + programName + " ===\n");
        resultText.append("Version: " + getLibraryVersion(programName) + "\n\n");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvResult.setText(resultText.toString());
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final String programPath = new File(getFilesDir(), programName).getAbsolutePath();
                    final String libPath = new File(getFilesDir(), "lib").getAbsolutePath();

                    resultText.append("Program path: " + programPath + "\n");
                    resultText.append("Lib path: " + libPath + "\n\n");
                    
                    // List files in lib directory to debug
                    File libDir = new File(libPath);
                    if (libDir.exists() && libDir.isDirectory()) {
                        resultText.append("Files in lib directory:\n");
                        String[] files = libDir.list();
                        if (files != null) {
                            for (String file : files) {
                                resultText.append("  " + file + "\n");
                            }
                        } else {
                            resultText.append("  No files\n");
                        }
                    } else {
                        resultText.append("Lib directory does not exist\n");
                    }
                    
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvResult.setText(resultText.toString());
                        }
                    });

                    final String[] command = {programPath};
                    ProcessBuilder processBuilder = new ProcessBuilder(command);
                    processBuilder.directory(new File(getFilesDir().getAbsolutePath()));
                    
                    java.util.Map<String, String> env = processBuilder.environment();
                    env.put("LD_LIBRARY_PATH", libPath);

                    resultText.append("Calling processBuilder.start()...\n");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvResult.setText(resultText.toString());
                        }
                    });

                    final Process process; 
                    try {
                        process = processBuilder.start();
                        resultText.append("Process started successfully\n");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvResult.setText(resultText.toString());
                            }
                        });
                    } catch (final Exception e) {
                        resultText.append("=== ERROR STARTING PROCESS ===\n");
                        resultText.append("Error: " + e.getMessage() + "\n");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvResult.setText(resultText.toString());
                            }
                        });
                        return;
                    }

                    final boolean[] processFinished = {false};

                    Thread stdoutThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            readStream(process.getInputStream(), resultText, "");
                        }
                    });

                    Thread stderrThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            readStream(process.getErrorStream(), resultText, "");
                        }
                    });

                    stdoutThread.start();
                    stderrThread.start();

                    final long startTime = System.currentTimeMillis();
                    final long timeout = 10000; // 10 seconds

                    while (!processFinished[0] && System.currentTimeMillis() - startTime < timeout) {
                        try {
                            int exitValue = process.exitValue();
                            processFinished[0] = true;
                            resultText.append("\n[PROCESS] Exited with code: " + exitValue + "\n");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tvResult.setText(resultText.toString());
                                }
                            });
                            break;
                        } catch (IllegalThreadStateException e) {
                        }
                        Thread.sleep(100);
                    }

                    if (!processFinished[0]) {
                        resultText.append("\n=== Timeout: Process took too long to complete ===\n");
                        process.destroy();
                        Thread.sleep(1000);
                        resultText.append("Process destroyed.\n");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvResult.setText(resultText.toString());
                            }
                        });
                    } else {
                        final int exitCode = process.waitFor();
                        stdoutThread.join();
                        stderrThread.join();
                        
                        resultText.append("\n=== Process Completed ===\n");
                        resultText.append("Exit code: " + exitCode + "\n");
                        if (exitCode == 0) {
                            resultText.append("Success!\n");
                        } else {
                            resultText.append("Error!\n");
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvResult.setText(resultText.toString());
                            }
                        });
                    }

                } catch (final Exception e) {
                    e.printStackTrace();
                    resultText.append("=== Error Occurred ===\n");
                    resultText.append("Error: " + e.getMessage() + "\n");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvResult.setText(resultText.toString());
                        }
                    });
                }
            }
        }).start();
    }
    
    private String getLibraryVersion(String programName) {
        switch (programName) {
            case "test-gmp":
                return "GMP 6.3.0";
            case "test-mpfr":
                return "MPFR 4.2.1";
            case "test-flint":
                return "FLINT 3.4.0";
            default:
                return "Unknown";
        }
    }
    
    private void copyLibraries() {
        try {
            // Create lib directory in app data directory
            File libDir = new File(getFilesDir(), "lib");
            if (!libDir.exists()) {
                libDir.mkdir();
            }
            
            // List of all library files to copy
            String[] libraries = {
                "libflint.so.22",
                "libgmp.so",
                "libmpfr.so"
            };
            
            // Copy each library from assets to app data directory
            for (String libName : libraries) {
                File libFile = new File(libDir, libName);
                
                try {
                    // Copy from assets/lib/arm64-v8a directory
                    java.io.InputStream inputStream = getAssets().open("lib/arm64-v8a/" + libName);
                    java.io.FileOutputStream outputStream = new java.io.FileOutputStream(libFile);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }
                    inputStream.close();
                    outputStream.close();
                    libFile.setReadable(true, false);
                    libFile.setExecutable(true, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            // Set LD_LIBRARY_PATH environment variable
            String libPath = libDir.getAbsolutePath();
            System.setProperty("java.library.path", libPath);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to copy libraries: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void runDixon() {
        final String input = etInput.getText().toString().trim();
        final String vars = etVars.getText().toString().trim();
        final String modulus = etModulus.getText().toString().trim();

        if (input.isEmpty() || vars.isEmpty() || modulus.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        final StringBuilder resultText = new StringBuilder();
        resultText.append("=== Starting Dixon Execution ===\n");
        resultText.append("Input: " + input + "\n");
        resultText.append("Vars: " + vars + "\n");
        resultText.append("Modulus: " + modulus + "\n\n");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvResult.setText(resultText.toString());
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final String dixonPath = new File(getFilesDir(), "dixon").getAbsolutePath();
                    final String libPath = new File(getFilesDir(), "lib").getAbsolutePath();

                    resultText.append("Dixon path: " + dixonPath + "\n");
                    resultText.append("Lib path: " + libPath + "\n\n");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvResult.setText(resultText.toString());
                        }
                    });

                    final String[] command = {dixonPath, input, vars, modulus};
                    ProcessBuilder processBuilder = new ProcessBuilder(command);
                    processBuilder.directory(new File(getFilesDir().getAbsolutePath()));
                    
                    java.util.Map<String, String> env = processBuilder.environment();
                    env.put("LD_LIBRARY_PATH", libPath);

                    resultText.append("Calling processBuilder.start()...\n");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvResult.setText(resultText.toString());
                        }
                    });

                    final Process process; 
                    try {
                        process = processBuilder.start();
                        resultText.append("Process started successfully\n");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvResult.setText(resultText.toString());
                            }
                        });
                    } catch (final Exception e) {
                        resultText.append("=== ERROR STARTING PROCESS ===\n");
                        resultText.append("Error: " + e.getMessage() + "\n");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvResult.setText(resultText.toString());
                            }
                        });
                        return;
                    }

                    final boolean[] processFinished = {false};

                    Thread stdoutThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            readStream(process.getInputStream(), resultText, "");
                        }
                    });

                    Thread stderrThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            readStream(process.getErrorStream(), resultText, "");
                        }
                    });

                    stdoutThread.start();
                    stderrThread.start();

                    final long startTime = System.currentTimeMillis();
                    final long timeout = 10000; // 10 seconds

                    while (!processFinished[0] && System.currentTimeMillis() - startTime < timeout) {
                        try {
                            int exitValue = process.exitValue();
                            processFinished[0] = true;
                            resultText.append("\n[PROCESS] Exited with code: " + exitValue + "\n");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tvResult.setText(resultText.toString());
                                }
                            });
                            break;
                        } catch (IllegalThreadStateException e) {
                        }
                        Thread.sleep(100);
                    }

                    if (!processFinished[0]) {
                        resultText.append("\n=== Timeout: Process took too long to complete ===\n");
                        process.destroy();
                        Thread.sleep(1000);
                        resultText.append("Process destroyed.\n");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvResult.setText(resultText.toString());
                            }
                        });
                    } else {
                        final int exitCode = process.waitFor();
                        stdoutThread.join();
                        stderrThread.join();
                        
                        resultText.append("\n=== Process Completed ===\n");
                        resultText.append("Exit code: " + exitCode + "\n");
                        if (exitCode == 0) {
                            resultText.append("Success!\n");
                        } else {
                            resultText.append("Error!\n");
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvResult.setText(resultText.toString());
                            }
                        });
                    }

                } catch (final Exception e) {
                    e.printStackTrace();
                    resultText.append("=== Error Occurred ===\n");
                    resultText.append("Error: " + e.getMessage() + "\n");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvResult.setText(resultText.toString());
                        }
                    });
                }
            }
        }).start();
    }

    private void clearInputs() {
        etInput.setText("");
        etVars.setText("");
        etModulus.setText("");
        tvResult.setText("");
    }
}
