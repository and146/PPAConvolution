PPA Project: Convolution matrix
===============================

WARNING:
--------

If this is a university's Git repository, you will be unable to build the apllication
because of missing local Maven repository containing the pre-built OpenCV native
libraries. To succesfully build and use this application, please download the sources
by cloning author's personal Git repository:

$ git clone https://github.com/and146/PPAConvolution.git

Documentation:
--------------

Javadoc: mvn javadoc:javadoc # see target/site/apidocs
Full docs: pdflatex doc/ppa_document.tex

Usage:
------

usage: uber-ConvolutionTool-1.0-SNAPSHOT.jar
 -d,--debug                         enables the debug mode (maximal
                                    verbosity)
 -f,--matrix-file <arg>             path to the XML containing the
                                    convolution matrices
 -h,--human-readable                enables the human readable time output
 -i,--input-image <arg>             the input image's filaname
 -l,--log <arg>                     enables the logging to a file
 -si,--show-input-image             show input image when loaded
 -so,--show-output-image            show input image(s) when loaded
 -wl,--measure-working-loops-only   measures only the working loops
   
