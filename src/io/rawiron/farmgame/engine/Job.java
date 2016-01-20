package io.rawiron.farmgame.engine;

import java.sql.ResultSet;
import java.sql.SQLException;

import io.rawiron.farmgame.data.DataStore;
import io.rawiron.farmgame.system.Logging;
import io.rawiron.farmgame.system.Trace;


public class Job {

    private Trace t;
    private Logging l;
    private DataStore ds;

    private Storage storage;
    private Reward reward;
    private Farmer farmer;
    private Building building;
    private Decoration decoration;

    public Job(DataStore in_ds, Logging in_l, Trace in_t) {
        t = in_t;
        ds = in_ds;
        l = in_l;
    }

    public void setStorage(Storage in_s) {
        storage = in_s;
    }

    public void setReward(Reward in_rw) {
        reward = in_rw;
    }

    public void setFarmer(Farmer in_f) {
        farmer = in_f;
    }

    public void setBuilding(Building in_bd) {
        building = in_bd;
    }

    public void setDecoration(Decoration in_d) {
        decoration = in_d;
    }


    public boolean testJobCompletion(String aFacebookUser, int in_farmID, int in_jobID, int in_recipeID) {
        return test(true, aFacebookUser, in_farmID, in_jobID, in_recipeID);
    }

    public boolean testRecipeCompletion(String aFacebookUser, int in_farmID, int in_jobID, int in_recipeID) {
        return test(false, aFacebookUser, in_farmID, in_jobID, in_recipeID);
    }


    private boolean test(boolean isJob, String aFacebookUser, int aFarmId, int aJobId, int aRecipeId)
/**
 * ABSTRACT
 * trigger( (b,q) and (d,q) and (r1,q1) and (r2,q2) and (r3,q3) and (r4,q4) and (r5,q5) ): swap(Pl,Jl)
 * trigger( (m,q) ):
 *
 * IN
 * isJob:Boolean
 * farmDBGroup, userDBGroup
 * user:String
 * farmID:int
 * jobID:int
 * recipeID:int
 *
 * OUT
 *
 * PERFORMANCE_IMPACT
 *	General:high
 *	Frequency:stress
 *	Cost:high
 *
 */
    {
        boolean response = false;

        int playerID = 0;
        playerID = farmer.getPlayerID(aFacebookUser);

        String db_sql_read_JobCounters_done = " SELECT DoneCount FROM JobCounters WHERE User=" + "'" + aFacebookUser + "'" + " AND JobID=" + aJobId;
        String db_sql_read_JobList = " SELECT RepeatLimit, Title "
                + ", BuildingRequirement, DecorationRequirement, BuildingQuantity, DecorationQuantity "
                + ", MasterySteps1, MasteryReward1, MasterySteps2, MasteryReward2, MasterySteps3, MasteryReward3, MasterySteps4, MasteryReward4, MasterySteps5, MasteryReward5 "
                + ", Requirement1, Quantity1, Requirement2, Quantity2, Requirement3, Quantity3 "
                + " FROM JobList WHERE JobList.JobID=" + aJobId;

        String db_sql_read_RecipeList = " SELECT Title "
                + ", Requirement1, Quantity1, Requirement2, Quantity2, Requirement3, Quantity3, Requirement4, Quantity4, Requirement5, Quantity5 "
                + " FROM RecipeList WHERE JobID=" + aRecipeId;


        ResultSet db_res_Task = null;
        ResultSet db_res_Count = null;
        boolean gotRequirements = false;
        int repeat = 0;
        int timesDone = 0;

        String title = null;
        String buildingRequirement = null;
        int buildingQuantity = 0;
        String decorationRequirement = null;
        int decorationQuantity = 0;

        class taskRequirement {
            public String what;
            public int howMuch;
        }
        class taskMastery {
            public String reward;
            public int steps;
        }
        taskRequirement[] requirements = {};
        taskMastery[] mastery = {};


        try {
            if (isJob) {
                db_res_Task = ds.query(db_sql_read_JobList, "read", aFacebookUser);
                db_res_Count = ds.query(db_sql_read_JobCounters_done, "write", aFacebookUser);

                if ((db_res_Task.next()) && (db_res_Count.next())) {
                    title = db_res_Task.getString("Title");
                    repeat = db_res_Task.getInt("RepeatLimit");
                    timesDone = db_res_Count.getInt("DoneCount");
                    buildingRequirement = db_res_Task.getString("BuildingRequirement");
                    buildingQuantity = db_res_Task.getInt("BuildingQuantity");
                    decorationRequirement = db_res_Task.getString("DecorationRequirement");
                    decorationQuantity = db_res_Task.getInt("DecorationQuantity");

                    gotRequirements = true;

                    // 0 means infinite so ignore.
                    if ((repeat > 0) && (timesDone >= repeat)) {
                        return response;
                    }
                }
            } else
            // => recipe
            {
                db_res_Task = ds.query(db_sql_read_RecipeList, "read", aFacebookUser);
                db_res_Task.next();

                gotRequirements = true;
            }
        } catch (SQLException e) {
        }

        if (gotRequirements) {
            try {
                requirements[0].howMuch = db_res_Task.getInt("Quantity1");
                requirements[0].what = db_res_Task.getString("Requirement1");
                requirements[1].howMuch = db_res_Task.getInt("Quantity2");
                requirements[1].what = db_res_Task.getString("Requirement2");
                requirements[2].howMuch = db_res_Task.getInt("Quantity3");
                requirements[2].what = db_res_Task.getString("Requirement3");
                requirements[3].howMuch = 0;
                requirements[3].what = null;
                requirements[4].howMuch = 0;
                requirements[4].what = null;

                if (!isJob) {
                    requirements[3].howMuch = db_res_Task.getInt("Quantity4");
                    requirements[3].what = db_res_Task.getString("Requirement4");
                    requirements[4].howMuch = db_res_Task.getInt("Quantity5");
                    requirements[4].what = db_res_Task.getString("Requirement5");
                }
            } catch (SQLException e) {
            }


            ResultSet db_res_Storage_requirement;
            boolean hasBuilding = false;
            boolean hasDecoration = false;
            boolean hasStorage = false;

            if (isJob) {
                // (b,q) and (d,q)
                //
                if (t.verbose && (t.verbose_level >= 3))
                    t.trace("variable dump " + buildingQuantity + " " + decorationQuantity);

                if (buildingQuantity > 0) {
                    hasBuilding = (building.count(aFacebookUser, playerID, buildingRequirement)) >= buildingQuantity;
                } else if (buildingQuantity == 0) {
                    hasBuilding = true;
                }

                if (hasBuilding && (decorationQuantity) > 0) {
                    hasDecoration = (decoration.count(aFacebookUser, playerID, decorationRequirement)) >= decorationQuantity;
                } else if (hasBuilding && decorationQuantity == 0) {
                    hasDecoration = true;
                }
                // POST
                // hasBuilding
                // hasDecoration
                if (t.verbose && (t.verbose_level >= 3)) t.trace("variable dump " + hasBuilding + " " + hasDecoration);


                // (r1,q1) and (r2,q2) and (r3,q3) and (r4,q4) and (r5,q5)
                // a single false evaluates the expression to false
                // all requirements are met when min(requirements)==1
                //
                String db_sql_read_Storage_requirement = " SELECT min(requirement) as check_true FROM ( "
                        + " SELECT if( (jl.Quantity1=0) or ((jl.Quantity1>0) and (jl.Quantity1 <= s.Quantity)), 1, 0) as requirement "
                        + " FROM Storage as s RIGHT JOIN JobList as jl ON (s.Contents=jl.Requirement1 AND s.FarmID=" + playerID + " AND s.Quantity>0 ) "
                        + " WHERE 1=1 "
                        + " AND JobID=" + aJobId
                        + " UNION ALL "
                        + " SELECT if( (jl.Quantity2=0) or ((jl.Quantity2>0) and (jl.Quantity2 <= s.Quantity)), 1, 0) as requirement "
                        + " FROM Storage as s RIGHT JOIN JobList as jl ON (s.Contents=jl.Requirement2 AND s.FarmID=" + playerID + " AND s.Quantity>0 ) "
                        + " WHERE 1=1 "
                        + " AND JobID=" + aJobId
                        + " UNION ALL "
                        + " SELECT if( (jl.Quantity3=0) or ((jl.Quantity3>0) and (jl.Quantity3 <= s.Quantity)), 1, 0) as requirement "
                        + " FROM Storage as s RIGHT JOIN JobList as jl ON (s.Contents=jl.Requirement3 AND s.FarmID=" + playerID + " AND s.Quantity>0 ) "
                        + " WHERE 1=1 "
                        + " AND JobID=" + aJobId
                        + ") as condition_check ";
                db_res_Storage_requirement = ds.query(db_sql_read_Storage_requirement, "write", aFacebookUser);
            } else {
                String db_sql_read_Storage_requirement2 = " SELECT min(requirement) as check_true FROM ( "
                        + " SELECT if( (jl.Quantity1=0) or ((jl.Quantity1>0) and (jl.Quantity1 <= s.Quantity)), 1, 0) as requirement "
                        + " FROM Storage as s RIGHT JOIN RecipeList as jl ON (s.Contents=jl.Requirement1 AND s.FarmID=" + playerID + " AND s.Quantity>0 ) "
                        + " WHERE 1=1 "
                        + " AND JobID=" + aJobId
                        + " UNION ALL "
                        + " SELECT if( (jl.Quantity2=0) or ((jl.Quantity2>0) and (jl.Quantity2 <= s.Quantity)), 1, 0) as requirement "
                        + " FROM Storage as s RIGHT JOIN RecipeList as jl ON (s.Contents=jl.Requirement2 AND s.FarmID=" + playerID + " AND s.Quantity>0 ) "
                        + " WHERE 1=1 "
                        + " AND JobID=" + aJobId
                        + " UNION ALL "
                        + " SELECT if( (jl.Quantity3=0) or ((jl.Quantity3>0) and (jl.Quantity3 <= s.Quantity)), 1, 0) as requirement "
                        + " FROM Storage as s RIGHT JOIN RecipeList as jl ON (s.Contents=jl.Requirement3 AND s.FarmID=" + playerID + " AND s.Quantity>0 ) "
                        + " WHERE 1=1 "
                        + " AND JobID=" + aJobId
                        + " UNION ALL "
                        + " SELECT if( (jl.Quantity4=0) or ((jl.Quantity4>0) and (jl.Quantity4 <= s.Quantity)), 1, 0) as requirement "
                        + " FROM Storage as s RIGHT JOIN RecipeList as jl ON (s.Contents=jl.Requirement4 AND s.FarmID=" + playerID + " AND s.Quantity>0 ) "
                        + " WHERE 1=1 "
                        + " AND JobID=" + aJobId
                        + " UNION ALL "
                        + " SELECT if( (jl.Quantity5=0) or ((jl.Quantity5>0) and (jl.Quantity5 <= s.Quantity)), 1, 0) as requirement "
                        + " FROM Storage as s RIGHT JOIN RecipeList as jl ON (s.Contents=jl.Requirement5 AND s.FarmID=" + playerID + " AND s.Quantity>0 ) "
                        + " WHERE 1=1 "
                        + " AND JobID=" + aJobId
                        + ") as condition_check ";
                db_res_Storage_requirement = ds.query(db_sql_read_Storage_requirement2, "write", aFacebookUser);
            }

            try {
                if (db_res_Storage_requirement.next() && (db_res_Storage_requirement.getInt("check_true") == 1))
                    hasStorage = true;
            } catch (SQLException e) {
            }


            if (((isJob && hasBuilding && hasDecoration) || (!isJob)) && hasStorage) {
                if (l.log) {
                    ds.execute("INSERT INTO `log` VALUES ( Now(), 'testCurrentJob','" + aFacebookUser + "','" + playerID + "','" + title + "',0, 'G',null,1 )"
                            , "log", null);
                }

                reward.give(title, aFacebookUser, aFarmId);

                // swap(Pl,Jl) := add(-Pl), add(Jl)
                //
                int i;
                for (i = 0; i < 5; i++) {
                    if (requirements[i].howMuch > 0)
                        storage.add(aFacebookUser, playerID, requirements[i].what, -requirements[i].howMuch);
                }

                // count task completion
                this.add(aFacebookUser, aJobId, 1);
                timesDone = timesDone + 1;

                if (isJob) {
                    i = 0;
                    while (i < 5 && timesDone > mastery[i].steps) {
                        i++;
                    }
                    if ((mastery[i].steps == timesDone) && (!mastery[i].reward.equals("0"))) {
                        reward.give(mastery[i].reward, aFacebookUser, aFarmId);
                    }
                }

                response = true;
            }
        }

        return response;
    }


    public int add(String aFacebookUser, int in_jobID, int in_amount)
/**
 * ABSTRACT
 * add(Jl)
 *
 * IN
 * user:String
 * jobID:int
 * amount:int
 * dbgroup:int
 *
 * PRE
 * JobCounters has unique key (User,JobID)
 *
 *
 * PERFORMANCE_IMPACT
 *	General:high
 *	Frequency:stress
 *	Cost:low
 */
    {
        int success = 0;
        success = ds.execute(" INSERT INTO JobCounters ( User, DoneCount, JobID ) VALUES ( '" + aFacebookUser + "'" + "," + in_amount + "," + in_jobID + ")"
                        + " ON DUPLICATE KEY UPDATE DoneCount=DoneCount+ FLOOR(" + in_amount + ")"
                , "write", aFacebookUser);

        return success;
    }


    public ResultSet retrieve(String aFacebookUser) {
        String sql =
                " SELECT JobList.*, DoneCount, RewardList.* "
                        + " FROM JobList INNER JOIN RewardList ON Title=RewardName "
                        + " LEFT JOIN  ( Select DoneCount FROM JobCounters WHERE User=" + "'" + aFacebookUser + "'" + ") AS A ON A.JobID = JobList.JobID";

        return ds.query(sql, "read", aFacebookUser);
    }

}
