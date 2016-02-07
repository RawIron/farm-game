from sqlalchemy import Column, Integer, String
from sqlalchemy.ext.declarative import declarative_base

Base = declarative_base


class AnimalList(Base):
    __tablename__ = "animal_list"

    player_id = Column(String(64), nullable=False)
    animal_id = Column(Integer, primary_key=True)
    animal_name = Column(String(128), nullable=False)
    last_harvest = Column(Integer)
    x = Column(Integer)
    y = Column(Integer)
