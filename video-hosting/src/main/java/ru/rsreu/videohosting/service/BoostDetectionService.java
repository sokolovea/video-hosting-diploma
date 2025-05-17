package ru.rsreu.videohosting.service;

import org.jgrapht.Graph;
import org.jgrapht.alg.cycle.JohnsonSimpleCycles;
import org.jgrapht.graph.*;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import ru.rsreu.videohosting.dto.MutualMarkPairWeightDto;
import ru.rsreu.videohosting.repository.UserCommentMarkRepository;
import ru.rsreu.videohosting.repository.UserVideoMarkRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BoostDetectionService {

    private final UserVideoMarkRepository userVideoMarkRepository;
    private final UserCommentMarkRepository userCommentMarkRepository;

    public BoostDetectionService(UserVideoMarkRepository userVideoMarkRepository, UserCommentMarkRepository userCommentMarkRepository) {
        this.userVideoMarkRepository = userVideoMarkRepository;
        this.userCommentMarkRepository = userCommentMarkRepository;
    }

    /**
     * Находит все простые циклы длины >= minLength и <= maxLength
     */
    public List<Pair<List<Long>, Long>> findUserMarkVideoCycles(int minLength, int maxLength) {
        // 1) Собираем ребра
        List<MutualMarkPairWeightDto> edges = userVideoMarkRepository.findMutualVideoMark(0)
                .stream()
                .map(p -> new MutualMarkPairWeightDto(p.getUserA(), p.getUserB(), p.getWeight()))
                .toList();
        return findCycles(edges, minLength, maxLength).stream().map(
                value -> Pair.of(value.getLeft(), Math.round(value.getRight()))
        ).collect(Collectors.toList());
    }

    public List<Pair<List<Long>, Long>> findUserMarkCommentCycles(int minLength, int maxLength) {
        // 1) Собираем ребра
        List<MutualMarkPairWeightDto> edges = userCommentMarkRepository.findMutualCommentMark(0)
                .stream()
                .map(p -> new MutualMarkPairWeightDto(p.getUserA(), p.getUserB(), p.getWeight()))
                .toList();
        return findCycles(edges, minLength, maxLength).stream().map(
                value -> Pair.of(value.getLeft(), Math.round(value.getRight()))
        ).collect(Collectors.toList());
    }

    private List<Pair<List<Long>, Double>> findCycles(List<MutualMarkPairWeightDto> edges, int minLength, int maxLength) {
        Graph<Long, DefaultWeightedEdge> graph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        edges.forEach(e -> {
            graph.addVertex(e.getUserA());
            graph.addVertex(e.getUserB());
            DefaultWeightedEdge edge = graph.addEdge(e.getUserA(), e.getUserB()); // Добавляем ребро
            if (edge != null) { // Проверяем, что ребро добавлено успешно
                graph.setEdgeWeight(edge, e.getWeight()); // Устанавливаем вес
            }
        });

        JohnsonSimpleCycles<Long, DefaultWeightedEdge> cycleDetector = new JohnsonSimpleCycles<>(graph);

        List<Pair<List<Long>, Double>> weightedCycles = cycleDetector.findSimpleCycles().stream()
                .map(cycle -> {
                    double weight = cycle.get(0);
                    for (int i = 0; i < cycle.size(); i++) {
                        Long from = cycle.get(i);
                        Long to = cycle.get((i + 1) % cycle.size()); // Следующая вершина или первая для замыкания
                        DefaultWeightedEdge edge = graph.getEdge(from, to);
                        if (edge != null) {
                            weight = Math.min(graph.getEdgeWeight(edge), weight);
                        }
                    }
                    return Pair.of(cycle, weight); // Пара: цикл и его "мощность"
                })
                .toList();

        weightedCycles.forEach(cycle -> {
            System.out.println("Cycle: " + cycle.getLeft() + ", Power: " + cycle.getRight());
        });


        // 4) Фильтрация по длине
        return weightedCycles.stream()
                .filter(cycle -> cycle.getLeft().size() >= minLength && cycle.getLeft().size() <= maxLength)
                .collect(Collectors.toList());  // возвращаем List<Pair<List<Long>, Double>>
    }

}


