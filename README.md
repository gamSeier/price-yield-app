# Bond Price-Yield Application

The application allows users to input par value, coupon, settlement date, maturity date and either price or yield. Once the bond attributes have been added, 
the application will determine price or yield, duration, convexity, tenor, benchmark treasury yield and bond spread. The benchmark treasury yield gets pulled via 
the API from the FRED web service. In addition to calculating the bond derivatives and spread, the application generates a price-yield chart based on the calculated
duration and convexity values.
