package app.simple.inure.singletons;

import java.util.HashSet;
import java.util.Set;

public class TrackerTags {
    
    private static Set <String> trackerPackages = new HashSet <>();
    
    public static synchronized void setTrackerPackages(Set <String> packages) {
        trackerPackages = packages;
    }
    
    public static synchronized void addPackage(String packageName) {
        trackerPackages.add(packageName);
    }
    
    public static synchronized void removePackage(String packageName) {
        trackerPackages.remove(packageName);
    }
    
    public static synchronized boolean isPackageTracked(String packageName) {
        return trackerPackages.contains(packageName);
    }
    
    public static synchronized Set <String> getTrackedPackages() {
        /*
         * In the getTrackedPackages() method, a new HashSet is
         * returned with the contents of trackerPackages. This
         * is to avoid any concurrent modification exceptions
         * that might occur if the set is modified while it's
         * being iterated over in another thread. This technique
         * is known as "defensive copying".
         */
        return new HashSet <>(trackerPackages);
    }
    
    public static synchronized void clear() {
        trackerPackages.clear();
    }
    
    public static synchronized void addAll(Set <String> packages) {
        trackerPackages.addAll(packages);
    }
    
    public static synchronized void removeAll(Set <String> packages) {
        trackerPackages.removeAll(packages);
    }
    
    public static synchronized boolean isEmpty() {
        return trackerPackages.isEmpty();
    }
    
    public static synchronized int size() {
        return trackerPackages.size();
    }
    
    public static synchronized boolean contains(String packageName) {
        return trackerPackages.contains(packageName);
    }
}
