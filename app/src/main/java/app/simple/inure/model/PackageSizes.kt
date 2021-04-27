package app.simple.inure.model

open class PackageSizes {

    /**
     * Size of the data stored by the current
     * package
     */
    var dataSize: Long = 0

    /**
     * Cache size of the current package
     */
    var cacheSize: Long = 0

    /**
     * APK size of the current package
     */
    var codeSize: Long = 0

    var externalDataSize: Long = 0
    var externalCacheSize: Long = 0
    var externalCodeSize: Long = 0
    var externalMediaSize: Long = 0
    var externalObbSize: Long = 0

    constructor(
            dataSize: Long,
            cacheSize: Long,
            codeSize: Long,
            externalDataSize: Long,
            externalCacheSize: Long,
            externalCodeSize: Long,
            externalMediaSize: Long,
            externalObbSize: Long,
    ) {
        this.dataSize = dataSize
        this.cacheSize = cacheSize
        this.codeSize = codeSize
        this.externalDataSize = externalDataSize
        this.externalCacheSize = externalCacheSize
        this.externalCodeSize = externalCodeSize
        this.externalMediaSize = externalMediaSize
        this.externalObbSize = externalObbSize
    }

    constructor(dataSize: Long, cacheSize: Long, codeSize: Long) {
        this.dataSize = dataSize
        this.cacheSize = cacheSize
        this.codeSize = codeSize
    }

    constructor()
}