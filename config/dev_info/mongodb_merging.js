var dbThatIsMerging = 'login';
var remoteHost = 'goldberglinux.tamu.edu:27017';

var importDb = connect("localhost:27017/" + dbThatIsMerging);
var exportDb = connect(remoteHost + '/' + dbThatIsMerging);

var collectionNames = exportDb.getCollectionNames();

printjson(collectionNames.length)

for (var i = 0; i < collectionNames.length; i++) {
    var collection = collectionNames[i];
    printjson(collection)
    // dynamically grab collections to sort
    cursor = exportDb[collection].find();
    while ( cursor.hasNext() ) {
        var object = cursor.next();
        // debug stuff
        //printjson( object );
        importDb[collection].insert(object);
    }
}
