IMG_DIR=images_cluster
echo Extracting features from images in $IMG_DIR
java -classpath feature/bin extract.ProcessImg -ofeature.txt $IMG_DIR

echo Run k-mean on extract features
java -classpath cluster/bin Parasol.Cluster feature.txt | tee cluster.txt

echo Removing old files/
rm -rf files/

echo Clustering image files
cluster_field=0
for ln in `cat cluster.txt`
do
    if [ $cluster_field -eq 0 ]; then
	filename=$ln
	echo -n "Filename: $filename"
	cluster_field=1
    else
	cluster=$ln
	echo " Cluster: $cluster"
	# copy file
	mkdir -p files/$cluster
	cp $filename files/$cluster
	cluster_field=0
    fi
done
echo Clustered files are now in files/

