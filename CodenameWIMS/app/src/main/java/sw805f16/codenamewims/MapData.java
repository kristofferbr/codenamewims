package sw805f16.codenamewims;

import java.util.ArrayList;

/**
 * Created by kbrod on 21/03/2016.
 * Class for generation and return of MapData from Føtex
 *
 */
public class MapData {


    ArrayList<WimsPoints> Data = new ArrayList<>();


    public MapData(){


        /* These are High Granularity data
        Data.add(new WimsPoints(180,485));
        Data.add(new WimsPoints(205,485));
        Data.add(new WimsPoints(225,485));
        Data.add(new WimsPoints(245,485));
        Data.add(new WimsPoints(270,485));
        Data.add(new WimsPoints(220,485));

        Data.add(new WimsPoints(180,460));
        Data.add(new WimsPoints(155,460));
        Data.add(new WimsPoints(137,460));
        Data.add(new WimsPoints(120,460));
        Data.add(new WimsPoints(205,460));
        Data.add(new WimsPoints(225,460));
        Data.add(new WimsPoints(250,460));
        Data.add(new WimsPoints(268,460));
        Data.add(new WimsPoints(320,460));
        Data.add(new WimsPoints(380,460));
        Data.add(new WimsPoints(180,435));
        Data.add(new WimsPoints(200,435));
        Data.add(new WimsPoints(220,435));
        Data.add(new WimsPoints(245,435));
        Data.add(new WimsPoints(270,435));
        Data.add(new WimsPoints(293,435));
        Data.add(new WimsPoints(330,435));
        Data.add(new WimsPoints(330,440));
        Data.add(new WimsPoints(350,440));
        Data.add(new WimsPoints(370,440));
        Data.add(new WimsPoints(380,440));

        Data.add(new WimsPoints(180,417));
        Data.add(new WimsPoints(147,417));
        Data.add(new WimsPoints(120,417));
        Data.add(new WimsPoints(203,417));
        Data.add(new WimsPoints(223,417));
        Data.add(new WimsPoints(246,417));

        Data.add(new WimsPoints(180,400));
        Data.add(new WimsPoints(180,343)); */


        Data.add(0,new WimsPoints(175,485));
        Data.add(1,new WimsPoints(175,346));

        Data.get(0).Neighbours.add(Data.get(1));
        Data.get(1).Neighbours.add(Data.get(0));

        Data.add(2,new WimsPoints(175,260));

        Data.get(2).Neighbours.add(Data.get(0));
        Data.get(0).Neighbours.add(Data.get(2));

        Data.add(3,new WimsPoints(258,257));

        Data.get(2).Neighbours.add(Data.get(3));
        Data.get(3).Neighbours.add(Data.get(2));

        Data.add(4,new WimsPoints(175,136));

        Data.get(4).Neighbours.add(Data.get(2));
        Data.get(2).Neighbours.add(Data.get(4));

        Data.add(5,new WimsPoints(260,134));

        Data.get(4).Neighbours.add(Data.get(5));
        Data.get(5).Neighbours.add(Data.get(4));

        Data.get(3).Neighbours.add(Data.get(5));
        Data.get(5).Neighbours.add(Data.get(3));

        Data.add(6,new WimsPoints(321,135));

        Data.get(6).Neighbours.add(Data.get(5));
        Data.get(5).Neighbours.add(Data.get(6));

        Data.add(7, new WimsPoints(320,35));

        Data.get(6).Neighbours.add(Data.get(7));
        Data.get(7).Neighbours.add(Data.get(6));

        Data.add(8,new WimsPoints(260,35));

        Data.get(5).Neighbours.add(Data.get(8));
        Data.get(8).Neighbours.add(Data.get(5));

        Data.get(7).Neighbours.add(Data.get(8));
        Data.get(8).Neighbours.add(Data.get(7));

        Data.add(9,new WimsPoints(180,32));

        Data.get(9).Neighbours.add(Data.get(8));
        Data.get(8).Neighbours.add(Data.get(9));

        Data.add(10, new WimsPoints(320, 256));

        Data.get(10).Neighbours.add(Data.get(3));
        Data.get(3).Neighbours.add(Data.get(10));

        Data.add(11, new WimsPoints(320,485));

        Data.get(11).Neighbours.add(Data.get(0));
        Data.get(0).Neighbours.add(Data.get(11));

        Data.get(11).Neighbours.add(Data.get(10));
        Data.get(10).Neighbours.add(Data.get(11));




    }





}
