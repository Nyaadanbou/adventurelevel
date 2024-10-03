plugins {
    id("nyaadanbou-conventions.repositories")
    id("adventurelevel-conventions")

}

dependencies {
    // internal
    compileOnly(project(":api"))
    compileOnly(local.paper)
    compileOnly(local.helper)

    // hooks
    compileOnly(local.luckperms)
    compileOnly(local.papi) { isTransitive = false }
    compileOnly(local.minipapi) { isTransitive = false }
}
