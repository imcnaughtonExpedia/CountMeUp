package com.bbc.countMeUp

import java.util.UUID

import com.bbc.countMeUp.dao.impl._
import com.bbc.countMeUp.domain.{CandidateDomain, ElectionDomain, UserDomain}
import org.scalatest.{FunSpec, Matchers}

import scala.util.Random


/*
 * Im going to leave this test in the github repo, normally I would have taken this out. and done performance
 * testing elsewhere but I suppose I should show my work, by modifying the number in the for loop below that
 * creates votes, I was able get numbers on how long it took to get election results with any number of random votes,
 *
 * Because my data store are behind a dao, this works with both the hash map and mongo data store.
 */
class perfTest extends FunSpec with Matchers{

  describe("testing"){
    it("perf test"){
      val userDomain = new UserDomain with InMemoryUserDao with InMemoryVoteDao with InMemoryElectionDao
      val electionDomain = new ElectionDomain with InMemoryElectionDao with InMemoryVoteDao with  InMemoryCandidateDao
      val candidateDomain = new CandidateDomain with InMemoryCandidateDao

      val candidate1 = candidateDomain.addCandidate("candidate1")
      val candidate2 = candidateDomain.addCandidate("candidate2")
      val candidate3 = candidateDomain.addCandidate("candidate3")
      val candidate4 = candidateDomain.addCandidate("candidate4")

      val candidateList = List(
        candidate1,
        candidate2,
        candidate3,
        candidate4
      )

      val maxVotesPerPerson = 5
      val election = electionDomain.addElection(
        Set(candidate1.id, candidate2.id, candidate3.id, candidate4.id),
        maxVotesPerPerson)

      var userVotes: Int = 0
      var currentUser = userDomain.addUser("user 0")

      for(x <- 1 to 100){
        if(userVotes >= maxVotesPerPerson){
          currentUser = userDomain.addUser("user " + x)
          userVotes = 0
        }

        val randomCandidateId: UUID = candidateList(Random.nextInt(candidateList.size)).id

        userVotes = userVotes + 1
        userDomain.voteInElection(currentUser.id, election.id, randomCandidateId)
        Console.print("\r" + x)
      }

      val startTime = System.currentTimeMillis()
      val electionResults = electionDomain.getElectionResults(election.id)
      val endTime = System.currentTimeMillis()

      Console.println(electionResults)
      Console.println((endTime - startTime) + " milli seconds runtime")
    }
  }

}
