package ru.rsreu.videohosting.service;

import org.jgrapht.Graph;
import org.jgrapht.alg.cycle.JohnsonSimpleCycles;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import ru.rsreu.videohosting.repository.UserVideoMarkRepository;

import java.util.List;
import java.util.stream.Collectors;

import org.jgrapht.Graph;
import org.jgrapht.alg.cycle.JohnsonSimpleCycles;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CycleDetectionService {

    private final UserVideoMarkRepository userVideoMarkRepository;

    public CycleDetectionService(UserVideoMarkRepository userVideoMarkRepository) {
        this.userVideoMarkRepository = userVideoMarkRepository;
    }

    /**
     * Находит все простые циклы длины >= minLength и <= maxLength
     */
    public List<List<Long>> findUserLikeCycles(int minLength, int maxLength) {
        // 1) Собираем ребра
        List<Pair<Long, Long>> edges = userVideoMarkRepository.findMutualVideoLikePairs()
                .stream()
                .map(p -> Pair.of(p.getUserA(), p.getUserB()))
                .toList();

        // 2) Строим направленный граф
        Graph<Long, DefaultEdge> graph = new DirectedMultigraph<>(DefaultEdge.class);
        edges.forEach(e -> {
            graph.addVertex(e.getLeft());
            graph.addVertex(e.getRight());
            graph.addEdge(e.getLeft(), e.getRight());
        });

        // 3) Поиск всех простых циклов
        JohnsonSimpleCycles<Long, DefaultEdge> cycleDetector = new JohnsonSimpleCycles<>(graph);
        List<List<Long>> allCycles = cycleDetector.findSimpleCycles();

        // 4) Фильтрация по длине
        return allCycles.stream()
                .filter(cycle -> cycle.size() >= minLength && cycle.size() <= maxLength)
                .collect(Collectors.toList());
    }
}


