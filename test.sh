IMG_DIR=compression/subimg/test_data
echo Extracting features from images in $IMG_DIR
java -classpath feature/bin extract.ProcessImg -ofeature.txt $IMG_DIR

echo Run k-mean on extract features
java -classpath cluster/bin Parasol.Cluster feature.txt | tee cluster.txt
