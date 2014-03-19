Memo:
-Operating data: file->db, db->point File
mo.umac.db.MainDB

-run Crawling algorithm:
mo.umac.crawler.MainCrawler

-open the openstreetmap
JOSM: org.openstreetmap.josm.gui.MainApplication

-revise how the openstreetmap operate the .pois file
JOSM: authorYYY.TopicImporterKate.java

-revise the operators supported by OSM
JOSM: authorYYY.updateByYYY
JOSM: config.properities

-change the color of the points
JOSM: authorYYY.Configs.java (topicWeights)
JOSM: org.openstreetmap.josm.Main.java
JOSM: org.openstreetmap.josm.gui.mappaint.SimpleNodeElemStyle.java 
JOSM: authorYYY.updateByYYY


TODO list
* change the online algorithm to the upper bound algorithm
* how to log the crawling results
* pause and resume downloads crawling

* analysis the results, draw the points on the map.
- record the crawling times of each point!


* implement the heuristic algorithm, how much it exceed the upper bound algorithm?
- stop issuing duplicate queries
- small circle: how to cover?
- should I use a stack instead of a queue? 

- read the .shp file line by line in src/test/java/mo.umac.external.uscensus.test.UScensusDataTest.java
x -parse glass website

- rtree for n dimensional data

