# farm-game

* fast hacked code
* use to see and learn how things can be done better
* everything is coded by hand, no packages
* no abstractions
* hard-coded SQL

Major purpose of the backend is to make data persistent.
Start with creating the tables that store the data of the game items.
Also store the game settings in tables.
Frontend uses synchronous calls because the backend is the authority.

The SQL statements do the work. Write a function around the SQL.
Each feature is a new function.
Code the required SQL statements, run them in the appropriate sequence and return the result.
Everything is synchronous.
A lot of Stop-and-Go: run a bit of code, wait for the database, run a bit of code, and so forth.

### do better
* exception handling. currently exceptions get eaten by many classes.
* database abstraction. python has the awesome sqlalchemy package. use something similar.
* session management.
* write tests.