Bioliq S-MAB data
=================

Bioliq S-MAB data (c) by Karlsruhe Institute of Technology

This archive contains the "Bioliq S-MAB data" for the real-world use case in the paper: 

- Edouard Fouché, Junpei Komiyama, and Klemens Böhm. 2019. Scaling Multi-Armed Bandit Algorithms. In The 25th ACM SIGKDD Conference on Knowledge Discovery and Data Mining (KDD ’19), August 4–8, 2019, Anchorage, AK, USA. ACM, New York, NY, USA, 11 pages. https://doi.org/10.1145/3292500.3330862

The data is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 4.0 (CC-BY-NC-SA-4.0) International License. Please see the `LICENSE` and `CC-BY-NC-SA-4.0` files. 

If you use any of the content of this archive. Please cite our paper. 

About the data
--------------

The data comes from a selection of 20 sensors in the Bioliq® pilot plant (see https://www.bioliq.de/english/) at KIT. It corresponds to one week of production, from the 2016-07-17 00:00:00 to 2016-07-23 23:59:59. The first column, 'Time', is the timestamp in the format YYYY-MM-DD HH:MM:SS. The others columns are the sensors: CP 32403 XQ01, CP 32202 XQ01, CL 32303 XQ01, HK 32303 XQ01, CQ 32501_2 XQ01, HK 32409 XQ01, CT 32732 XQ01, CF 32915 XQ01, HK 32406 XQ01, CL 32402 XQ01, CT 32213 XQ01, CT 32402 XQ01, TV 32408 XQ01, CT 32716 XQ01, CF 32501 XQ01, CP 32704 XQ01, CF 32923 XQ01, CF 32201 XQ01, CP 32100 XQ01, CQ 32305 XQ01. 

The basic semantic of the sensors is as follows: 
- CP 32403 XQ01: Pressure (bar) 
- CP 32202 XQ01: Pressure (mbar)
- CL 32303 XQ01: Filling level (%)
- HK 32303 XQ01: Temperature (°C)
- CQ 32501_2 XQ01: CO gas analysis (vpm)
- HK 32409 XQ01: Temperature (°C)
- CT 32732 XQ01: Temperature (°C)
- CF 32915 XQ01: Flow (Nm³/h)
- HK 32406 XQ01: Temperature (°C)
- CL 32402 XQ01: Filling level (%)
- CT 32213 XQ01: Temperature (°C)
- CT 32402 XQ01: Temperature (°C)
- TV 32408 XQ01: Relative temperature (%)
- CT 32716 XQ01: Temperature (°C)
- CF 32501 XQ01: Flow (m³/h)
- CP 32704 XQ01: Pressure (bar)
- CF 32923 XQ01: Flow (Nm³/h)
- CF 32201 XQ01: Flow (m³/h)
- CP 32100 XQ01: Druck (mbar)
- CQ 32305 XQ01: CO gas analysis (ppm)

A more extensive description of the sensor semantic will follow in further releases. 

Originally, the sensor values are collected independently at the plan, and a new value is registred only if the sensor value differs by some threshold from the previous one. The raw data was interpolated at the granularity of a second via the so-called zero-interpolation. A minor gaussian noise (of magnitude 10^-4) was added afterward to account for measurement imprecisions and avoid the occurence of attributes keeping the exact same value over long time spans. 


